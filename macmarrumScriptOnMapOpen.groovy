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
            String attrName
            List<String[]> listOfNameScript = new LinkedList<>()
            for (Attribute attr : attributeTable.getAttributes()) {
                attrName = attr.getName()
                if (attrName.toLowerCase().startsWith(ATTRIBUTE_NAME.toLowerCase())) {
                    script = (String) attr.getValue()
                    if (script != null)
                        listOfNameScript.add(new String[]{attrName, script})
                }
            }
            if (executeWithoutAsking == ASK && listOfNameScript.size() > 0) {
                String message = String.format("Execute scriptOnMapOpen for\n%s?", mapDescription)
                int response = UITools.showConfirmDialog(null, message, "Execute scriptOnMapOpen?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                if (response != 0)
                    return
            }
            int i = 0
            for (String[] attrName_script : listOfNameScript) {
                attrName = attrName_script[0]
                script = attrName_script[1]
                def messageAboutExecuting = String.format("executing %s (#%d) in \"%s\"", attrName, ++i, mapDescription)
                LogUtils.info(messageAboutExecuting)
                try {
                    ScriptingEngine.executeScript(root, script)
                } catch (ExecuteScriptException e) {
                    LogUtils.severe(e)
                    String messageAboutError = String.format("Error %s\n\n%s", messageAboutExecuting, e.message)
                    UITools.showMessage(messageAboutError, JOptionPane.ERROR_MESSAGE)
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
