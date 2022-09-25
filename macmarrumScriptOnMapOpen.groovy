package io.github.macmarrum.freeplane

import org.freeplane.core.util.LogUtils
import org.freeplane.features.attribute.Attribute
import org.freeplane.features.attribute.NodeAttributeTableModel
import org.freeplane.features.map.IMapLifeCycleListener
import org.freeplane.features.map.MapModel
import org.freeplane.features.map.NodeModel
import org.freeplane.features.mode.Controller
import org.freeplane.plugin.script.ExecuteScriptException
import org.freeplane.plugin.script.ScriptingEngine

import java.text.MessageFormat

class MapCreationListener implements IMapLifeCycleListener {

    @Override
    void onCreate(MapModel map) {
        ScriptOnMapOpen.executeScriptOnMapOpen(map)
    }
}

class ScriptOnMapOpen {
    public static final String SCRIPT_ON_MAP_OPEN_ATTR_NAME = "scriptOnMapOpen"

    static void executeScriptOnMapOpen(MapModel map) {
        NodeModel root = map.getRootNode()
        NodeAttributeTableModel attributeTable = root.getExtension(NodeAttributeTableModel.class)
        if (attributeTable != null) {
            final File mapFile = map.getFile()
            final String mapDescription = mapFile != null ? mapFile.getName() : root.getText()
            String script
            int i = 0
            for (Attribute attr : attributeTable.getAttributes()) {
                if (attr.getName().toLowerCase().startsWith(SCRIPT_ON_MAP_OPEN_ATTR_NAME.toLowerCase())) {
                    script = (String) attr.getValue()
                    if (script != null) {
                        LogUtils.info(MessageFormat.format("executing {0} (#{1}) in \"{2}\"", attr.getName(), ++i, mapDescription))
                        try {
                            ScriptingEngine.executeScript(root, script)
                        } catch (ExecuteScriptException e) {
                            LogUtils.severe(e)
                        }
                    }
                }
            }
        }
    }
}

Controller controller = Controller.getCurrentController()
controller.getMapViewManager().getMaps().values().stream().distinct().forEach(ScriptOnMapOpen::executeScriptOnMapOpen)
controller.addMapLifeCycleListener(new MapCreationListener())
