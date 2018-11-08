Installer Anleitung

Für das packaging des Projekt wird das Programm Install4j genutzt. 
Dies erstellt einen Installer für alle gängigen Plattformen.

Das Programm ist hier zu finden:

https://www.ej-technologies.com/download/install4j/files

hierfür müsste noch eine Lizenz besorgt werden.

Install4J

Nach dem öffnen von Install4j wird der Nutzer durch einige Formulare geleitet.

Falls bereits ein Profil bestehet, kann dies über den menüpunkt „Open Project“ geladen werden (Gsehen_conf.install4j). Wichtig!!!! Beim nutzen des Projekts müssen die Formulare gepflegt werden bzw. Pfade angepasst werden.

In den „General Settings“ werden grundlegende Einstellungen getroffen, wie Name und Herausgeber der Software. Ebenso wichtig muss dort auch die Minimale Java Version angegeben werden. Es können verschiedene Sprachen für den Installer hinzugefügt werden, und je nach Location wird die passende Sprache ausgeführt. Bei dem Punkt „Media File Options“ kann das Output directory angegeben werden, dieser gibt an, wo am Ende der Kompilierte Installer gespeichert wird. Die anderen Formulare können weitestgehend ignoriert werden und nur noch bei „Project Options“ sollte bei : Make all paths relative … ein Häkchen gesetzt werden.
Unter dem Menüpunkt „Files“ muss der Pfad zum projekt angegeben werden.
 
Dies wird über das grüne + eingeleitet. Es muss das passende Directory geschehen. Nach Auswahl des Projekts im Workspace wird im Dialog Select excluded files and subdirectories ein Ordner System angezeigt. Dort können Dateien und Ordner angegeben werden, welche nicht am Ende in den Installierten Dateien enthalten sein sollten. Alle anderen eingaben können ignoriert werden bis zum Launcher. 
Es sollte ein neuer Launcher erstellt werden. Nach ausführen der Option „New launcher“ wird ein neues Dialogfenster geöffnet. Der launcher type sollte grundsätzlich ein Generated launcher sein. Beim nächsten Punkt wird die Konfiguration für die Executable angegeben.

 
Dies kann so beibehalten werden, da der Pfad bereits hinterlegt wurde (Wichtig!!! „.“ Bei Directory).
Im nächsten Dialog kann ein Icon für die Executable hinterlegt werden (im vorhandenen Profil nicht enthalten). Bei der Cunfigure Java invocation sollten nun folgende Class paths angegeben werden: 
Ein Directory muss hinzugefügt werden. Das „Directory bin“ muss so ausgewählt werden damit der Class path die Klasse automatisch findet und nur ausgewählt werden muss. Es müssen Scan directory (nur für Jar) hinzugefügt werden, welche die benötigten libs angibt. Ebenso muss das Argument -cp angegeben werden. Alle anderen Dialoge können so beibehalten werden und der Launcher somit abgeschlossen werden.
Im Menü Installer kann das aussehen des Installer Wizards verändert werden und Optionen angegeben werden wie: Desktop Icon erstellen usw. Dieser menüpunkt kann aber auch so standardmäßig bleiben.
Das Menü Media sorgt für das jeweilige Betriebssystem und es müssen somit jeweils eins für jedes Betriebssystem erstellt werden. 

Windows:
Die Auswahl „Installer“ wird nur für Windows benötigt. Dort muss der Installer type auf Windows eingestellt werden.
Die einzigen weiteren benötigten Einstellungen werden im Untermenü Bundled JRE angegeben.
Dort muss ein Bundle aus dem Dropdown gewählt werden. Der Bundle Type sollte ein Static bundle sein.
 
Unix/Linux:
Bei Unix wird eine Archive angelegt.
Nun können die weiteren punkte ignoriert werden bis zum Bundled JRE welche im Dropdown ausgewählt werden kann (Macosx JRE).

macOS:
Für den Mac sollte ebenfalls ein Archive gewählt werden.
Hier kann bis zur Auswahl des Lauschers durchgeklickt werden. Es muss der vorher erstellte Launcher nun im Dropdown ausgewählt werden.
Als Format sollte das DMG Format gewählt werden, da dies das üblichste für Mac ist. Ebenso muss wieder ein Bundled JRE angegeben werden.

Nach der Erstellung der Medias, kann nun ein build gestartet werden (die Medias auswählen, falls nicht Build all ausgewählt ist).

