This read me is purely for the developer.

To support a new version of Minecraft, use the following steps:
1. Copy a previous module and paste it in the main folder.
2. Right-click a previous module and access Open Module Settings.
3. Select + to add the new module to the list.
4. Rename this module to the desired module name (this is not the folder name).
5. Open its Pom and change artifactID to new version
6. Add module to the parent project pom
7. Access the Maven runtime by clicking the Maven icon on the right.
8. Add the new module to the list.
9. Click Lifecycle and clean.
10. Update dependency versions (remapped minecraft version from md5)
11. Also add the new module as dependency to mapcontrol module pom
12. And finally, don't forget item NBT dependency version
