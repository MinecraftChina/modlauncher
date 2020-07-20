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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

import static cpw.mods.modlauncher.LogMarkers.*;

/**
 * Decorates {@link cpw.mods.modlauncher.api.ITransformationService} to track state and other runtime metadata.
 */
public class TransformationServiceDecorator {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ITransformationService service;
    private boolean isValid;

    TransformationServiceDecorator(ITransformationService service) {
        this.service = service;
    }

    void onLoad(IEnvironment env, Set<String> otherServices) {
        try {
            LOGGER.debug(MODLAUNCHER,"Loading service {}", this.service::name);
            this.service.onLoad(env, otherServices);
            this.isValid = true;
            LOGGER.debug(MODLAUNCHER,"Loaded service {}", this.service::name);
        } catch (IncompatibleEnvironmentException e) {
            LOGGER.error(MODLAUNCHER,"Service failed to load {}", this.service.name(), e);
            this.isValid = false;
        }
    }

    boolean isValid() {
        return isValid;
    }

    void onInitialize(IEnvironment environment) {
        LOGGER.debug(MODLAUNCHER,"Initializing transformation service {}", this.service::name);
        this.service.initialize(environment);
        LOGGER.debug(MODLAUNCHER,"Initialized transformation service {}", this.service::name);
    }

    public void gatherTransformers(TransformStore transformStore) {
        LOGGER.debug(MODLAUNCHER,"Initializing transformers for transformation service {}", this.service::name);
        final List<ITransformer> transformers = this.service.transformers();
        Objects.requireNonNull(transformers, "The transformers list should not be null");
        final Map<Type, List<ITransformer>> transformersByType = transformers.stream().collect(Collectors.groupingBy(
                t ->
                {
                    final Type[] genericInterfaces = t.getClass().getGenericInterfaces();
                    for (Type typ : genericInterfaces) {
                        ParameterizedType pt = (ParameterizedType) typ;
                        if (pt.getRawType().equals(ITransformer.class)) {
                            return pt.getActualTypeArguments()[0];
                        }
                    }
                    throw new RuntimeException("How did a non-transformer get here????");
                }
        ));
        for (Type type : transformersByType.keySet()) {
            final TransformTargetLabel.LabelType labelType = TransformTargetLabel.LabelType.getTypeFor(type).orElseThrow(() -> new IllegalArgumentException("Invalid transformer type found"));
            for (ITransformer<?> xform : transformersByType.get(type)) {
                final Set<ITransformer.Target> targets = xform.targets();
                if (targets.isEmpty()) continue;
                final Map<TransformTargetLabel.LabelType, List<TransformTargetLabel>> labelTypeListMap = targets.stream().map(TransformTargetLabel::new).collect(Collectors.groupingBy(TransformTargetLabel::getLabelType));
                if (labelTypeListMap.keySet().size() > 1 || !labelTypeListMap.keySet().contains(labelType)) {
                    LOGGER.error(MODLAUNCHER,"Invalid target {} for transformer {}", labelType, xform);
                    throw new IllegalArgumentException("The transformer contains invalid targets");
                }
                labelTypeListMap.values().stream().flatMap(Collection::stream).forEach(target -> transformStore.addTransformer(target, xform, service));
            }
        }
        LOGGER.debug(MODLAUNCHER,"Initialized transformers for transformation service {}", this.service::name);
    }

    ITransformationService getService() {
        return service;
    }

    private void IntialNeteaseCoreModManager()
    {
        try {
            //add coremanager
            Class clazz = Class.forName("net.minecraftforge.coremod.CoreModEngine");
            Field f = clazz.getDeclaredField("ALLOWED_CLASSES");
            f.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

            List<String> classes = (List<String>)f.get(null);
            List classlist = new ArrayList(classes);
            classlist.add("com.netease.mc.mod.coremod.CoreModManager");
            f.set(null, classlist);
        } catch (Exception e) {
            LogManager.getLogger().error(e.toString());
            return;
        }
    }

    void runScan(final Environment environment) {
        LOGGER.debug(MODLAUNCHER,"Beginning scan trigger - transformation service {}", this.service::name);
        IntialNeteaseCoreModManager();
        this.service.beginScanning(environment);
        LOGGER.debug(MODLAUNCHER,"End scan trigger - transformation service {}", this.service::name);
    }
}
