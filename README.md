# Script Execution On Mindmap Open

Freeplane has the functionality to execute Groovy scripts when the application starts. It's known as "init scripts" → https://docs.freeplane.org/?search=init

This add-on extends the functionality with the ability to execute scripts when a mindmap is opened, i.e. individually for each mindmap.

To execute a script for a mindmap, in your root node add an attribute named "scriptOnMapOpen", then use `Tools->Edit script...` to enter the Groovy code.

_Note: The attribute name must start with "scriptOnMapOpen" but can have any suffix_

## Download

[**ScriptExecutionOnMindmapOpen.addon.mm**](../../releases/)
