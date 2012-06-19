package com.taskadapter.web.service;

import com.taskadapter.LegacyConnectorsSupport;
import com.taskadapter.PluginsFileParser;
import com.taskadapter.connector.definition.Descriptor;
import com.taskadapter.web.PluginEditorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Skorokhodov
 */
public class EditorManager {
    private final Logger logger = LoggerFactory.getLogger(EditorManager.class);

    private Map<String, PluginEditorFactory> editorFactories = new HashMap<String, PluginEditorFactory>();

    public EditorManager() {
        loadEditors();
    }

    private void loadEditors() {
        try {
            Collection<String> classNames = new PluginsFileParser().parseResource("editors.txt");
            for (String factoryClassName : classNames) {
                Class<PluginEditorFactory> factoryClass = (Class<PluginEditorFactory>) Class.forName(factoryClassName);
                PluginEditorFactory pluginFactory = factoryClass.newInstance();
                Descriptor descriptor = pluginFactory.getDescriptor();
                editorFactories.put(descriptor.getID(), pluginFactory);
            }
        } catch (Exception e) {
            logger.error("Loading editors: " + e.getMessage(), e);
        }
    }

    public PluginEditorFactory getEditorFactory(String pluginId) {
        String realId = LegacyConnectorsSupport.getRealId(pluginId);
        return editorFactories.get(realId);
    }

}
