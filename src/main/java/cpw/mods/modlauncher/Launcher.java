/*
 * ModLauncher - for launching Java programs with in-flight transformation ability.
 *
 *     Copyright (C) 2017-2019 cpw
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cpw.mods.modlauncher;

import cpw.mods.modlauncher.api.*;
import cpw.mods.modlauncher.serviceapi.*;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

import static cpw.mods.modlauncher.LogMarkers.*;
import com.netease.mc.mod.network.common.GameHost;
import com.netease.mc.mod.network.socket.NetworkSocket;
import java.lang.reflect.Field;

/**
 * Entry point for the ModLauncher.
 */
public class Launcher {
    public static Launcher INSTANCE;
    private final TypesafeMap blackboard;
    private final TransformationServicesHandler transformationServicesHandler;
    private final Environment environment;
    private final TransformStore transformStore;
    private final NameMappingServiceHandler nameMappingServiceHandler;
    private final ArgumentHandler argumentHandler;
    private final LaunchServiceHandler launchService;
    private final LaunchPluginHandler launchPlugins;
    public TransformingClassLoader classLoader;

    private Launcher() {
        INSTANCE = this;
        LogManager.getLogger().info(MODLAUNCHER,"ModLauncher {} starting: java version {} by {}", ()->IEnvironment.class.getPackage().getImplementationVersion(),  () -> System.getProperty("java.version"), ()->System.getProperty("java.vendor"));
        this.launchService = new LaunchServiceHandler();
        this.blackboard = new TypesafeMap();
        this.environment = new Environment(this);
        environment.computePropertyIfAbsent(IEnvironment.Keys.MLSPEC_VERSION.get(), s->IEnvironment.class.getPackage().getSpecificationVersion());
        environment.computePropertyIfAbsent(IEnvironment.Keys.MLIMPL_VERSION.get(), s->IEnvironment.class.getPackage().getImplementationVersion());
        environment.computePropertyIfAbsent(IEnvironment.Keys.MODLIST.get(), s->new ArrayList<>());
        environment.computePropertyIfAbsent(IEnvironment.Keys.SECURED_JARS_ENABLED.get(), k->SecureJarHandler.canHandleSecuredJars());
        this.transformStore = new TransformStore();
        this.transformationServicesHandler = new TransformationServicesHandler(this.transformStore);
        this.argumentHandler = new ArgumentHandler();
        this.nameMappingServiceHandler = new NameMappingServiceHandler();
        this.launchPlugins = new LaunchPluginHandler();
    }
	
	private void IntialSocket(String... args)
    {
        try {
            //初始化Socket
            Class t = Class.forName(GameHost.class.getName(), true, classLoader);
            Class[] argsClass = new Class[1];
            argsClass[0] = args.getClass();
            t.getMethod("Init", argsClass).invoke(null, new Object[]{args});
            t = Class.forName(NetworkSocket.class.getName(), true, classLoader);
            argsClass = new Class[0];
            t.getMethod("init", argsClass).invoke(null);
        } catch (Exception e) {
            LogManager.getLogger().error(e.toString());
            return;
        }
    }

    private void IntialNeteaseCoreModManager()
    {
        try {
            //add coremanager
            Class engine = Class.forName("net.minecraftforge.coremod.CoreModEngine", true, classLoader);
            Field f = engine.getField("ALLOWED_CLASSES");
            f.setAccessible(true);
            List<String> classes = (List<String>)f.get(null);
            classes.add("com.netease.mc.mod.coremod.CoreModManager");
            List<String> classlist = (List<String>)f.get(null);
            LogManager.getLogger().info("IntialSocket:" + classlist.toString());
        } catch (Exception e) {
            LogManager.getLogger().error(e.toString());
            return;
        }
    }


    public static void main(String... args) {
        ValidateLibraries.validate();
        LogManager.getLogger().info(MODLAUNCHER,"ModLauncher running: args {}", () -> LaunchServiceHandler.hideAccessToken(args));
        new Launcher().run(args);
    }

    public final TypesafeMap blackboard() {
        return blackboard;
    }

    private void run(String... args) {
        final Path gameDir = this.argumentHandler.setArgs(args);
        this.transformationServicesHandler.discoverServices(gameDir);
        final List<Map.Entry<String, Path>> scanResults = this.transformationServicesHandler.initializeTransformationServices(this.argumentHandler, this.environment, this.nameMappingServiceHandler);
        this.launchPlugins.offerScanResultsToPlugins(scanResults);
        this.launchService.validateLaunchTarget(this.argumentHandler);
        final TransformingClassLoaderBuilder classLoaderBuilder = this.launchService.identifyTransformationTargets(this.argumentHandler);
        this.classLoader = this.transformationServicesHandler.buildTransformingClassLoader(this.launchPlugins, classLoaderBuilder, this.environment);
        Thread.currentThread().setContextClassLoader(this.classLoader);
        IntialSocket(args);
		this.launchService.launch(this.argumentHandler, this.classLoader, this.launchPlugins);
    }

    public Environment environment() {
        return this.environment;
    }

    Optional<ILaunchPluginService> findLaunchPlugin(final String name) {
        return launchPlugins.get(name);
    }

    Optional<ILaunchHandlerService> findLaunchHandler(final String name) {
        return launchService.findLaunchHandler(name);
    }

    Optional<BiFunction<INameMappingService.Domain, String, String>> findNameMapping(final String targetMapping) {
        return nameMappingServiceHandler.findNameTranslator(targetMapping);
    }
}
