package io.github.macmarrum.freeplane

import org.freeplane.core.resources.ResourceController
import org.freeplane.core.ui.components.UITools
import org.freeplane.core.util.LogUtils
import org.freeplane.features.attribute.Attribute
import org.freeplane.features.attribute.NodeAttributeTableModel
import org.freeplane.features.map.IMapLifeCycleListener
import org.freeplane.features.map.MapModel
import org.freeplane.features.map.NodeModel
import org.freeplane.features.mode.Controller
import org.freeplane.plugin.script.ExecuteScriptException
import org.freeplane.plugin.script.ScriptingEngine

import javax.swing.JOptionPane

class MapCreationListener implements IMapLifeCycleListener {

    @Override
    void onCreate(MapModel map) {
        ScriptOnMapOpen.executeScriptOnMapOpen(map)
    }
}

class ScriptOnMapOpen {
    public static final String ATTRIBUTE_NAME = "scriptOnMapOpen"
    public static final String OPTION_NAME = "ScriptOnMapOpen.execute_without_asking"
    private static final String TRUE = "true"
    private static final String ASK = "ask"
    private static Map allowedForMap = new HashMap<MapModel, Boolean>()
    private static final String CONFIRMATION_MESSAGE_FORMAT = "Execute scriptOnMapOpen for\n%s?"
    private static final String CONFIRMATION_TITLE = "Execute scriptOnMapOpen?"

    static void executeScriptOnMapOpen(MapModel map) {
        String executeWithoutAsking = ResourceController.getResourceController().getProperty(OPTION_NAME, ASK)
        if (!(executeWithoutAsking == ASK || executeWithoutAsking == TRUE))
            return
        NodeModel root = map.getRootNode()
        NodeAttributeTableModel attributeTable = root.getExtension(NodeAttributeTableModel.class)
        if (attributeTable != null) {
            final File mapFile = map.getFile()
            final String mapDescription = mapFile != null ? mapFile.getName() : "not-yet-saved mindmap with root: " + root.getText()
            String script
            int i = 0
            for (Attribute attr : attributeTable.getAttributes()) {
                if (attr.getName().toLowerCase().startsWith(ATTRIBUTE_NAME.toLowerCase())) {
                    if (executeWithoutAsking == ASK) {
                        if (!allowedForMap.containsKey(map)) {
                            String message = String.format(CONFIRMATION_MESSAGE_FORMAT, mapDescription)
                            int response = UITools.showConfirmDialog(root, message, CONFIRMATION_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                            boolean isExecutionAllowed = response == 0
                            allowedForMap.put(map, isExecutionAllowed)
                            if (!isExecutionAllowed)
                                return
                        } else if (!allowedForMap.get(map))
                            return
                    }
                    script = (String) attr.getValue()
                    if (script != null) {
                        LogUtils.info(String.format("executing %s (#%d) in \"%s\"", attr.getName(), ++i, mapDescription))
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

def controller = Controller.currentController
def maps = controller.mapViewManager.maps.values().toSet()
for (map in maps) {
    ScriptOnMapOpen.executeScriptOnMapOpen(map)
}
controller.addMapLifeCycleListener(new MapCreationListener())
