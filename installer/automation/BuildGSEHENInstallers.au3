#include <MsgBoxConstants.au3>

Const $install4jDownloadPage = "https://www.ej-technologies.com/download/install4j/files"
Const $install4jName = "Install4J"
Const $install4jFolderName = "install4j8"
Const $install4jWindowTitleRE = "^install4j\b.*\bEdition( \[.*\])?$"
Const $install4jWindowTitle = "install4j Multi-Platform Edition"
Const $gsehenFolderName = "GSEHEN"

Func Install()
  Run(@ComSpec & " /c start " & $install4jDownloadPage, "", @SW_HIDE)
  Sleep(3000)
  If MsgBox(BitOR($MB_ICONINFORMATION, $MB_RETRYCANCEL), "Note", "Please download and install " & $install4jName & " for Windows (64-bit)," & @CRLF & _
      Chr(34) & "Setup Executable with JRE" & Chr(34) & "," & @CRLF & "from the web site just opened, then close this dialog") <> $IDRETRY Then
    Exit
  EndIf
EndFunc ; ===> Install()

Func AddLocationIfExists($locations, $location)
  If FileExists($location) Then
    Return ($locations = "" ? "" : $locations & @CRLF) & $location
  Else
    Return $locations
  EndIf
EndFunc ; ===> AddLocationIfExists()

Func ShowLocationChoiceDialog($locations, $description, $cancelHint, $inputDefault, ByRef $locationsArray)
  $dialogText = "E"
  If $locations <> "" Then
    $locationsArray = StringSplit(StringStripCR($locations), @LF)
    $locationsWithNumbers = ""
    For $i = 1 To $locationsArray[0]
      $locationsWithNumbers = $locationsWithNumbers & $i & "  " & $locationsArray[$i] & @CRLF
    Next
      $dialogText = "These " & $description & " have been found:" & _
      @CRLF & @CRLF & $locationsWithNumbers & @CRLF & @CRLF & "Enter the number of the path to use," & @CRLF & "or e"
  EndIf
  $inputBoxResult = InputBox("Input required", $dialogText & "nter the location manually here," & @CRLF & _
    "or press ESC / Cancel " & $cancelHint, $inputDefault, "", -1, 250)
  If @error Then
    Return ""
  Else
    Return $inputBoxResult
  EndIf
EndFunc ; ===> ShowLocationChoiceDialog()

Func EvaluateChoice($locationChoice, $locationsArray)
  $j = 1 * $locationChoice
  If $j = 0 Then
    Return $locationChoice
  Else
    Return $locationsArray[$j]
  EndIf
EndFunc ; ===> EvaluateChoice()

Func GetInstall4jLocation()
  $install4jLocation = ""
  Dim $install4jLocationsArray[0]
  Do
    $install4jLocations = AddLocationIfExists("", @ProgramFilesDir & "\" & $install4jFolderName)
    $install4jLocations = AddLocationIfExists($install4jLocations, (StringRegExp(@ProgramFilesDir, " \(x86\)$") _
      ? StringRegExpReplace(@ProgramFilesDir, " \(x86\)$", "") : @ProgramFilesDir & " (x86)") & "\" & $install4jFolderName)
    $install4jLocationChoice = ShowLocationChoiceDialog($install4jLocations, $install4jName & " installations", _
      "to newly install " & $install4jName, EnvGet("SystemDrive") & "\", $install4jLocationsArray)
    If $install4jLocationChoice = "" Then
      Install()
    Else
      $install4jLocation = EvaluateChoice($install4jLocationChoice, $install4jLocationsArray)
    EndIf
  Until $install4jLocation <> ""
  If Not FileExists($install4jLocation & "\bin\install4j.exe") Then
    Run(@ComSpec & " /c explorer " & Chr(34) & $install4jLocation & Chr(34), "", @SW_HIDE)
    Sleep(1000)
    MsgBox($MB_ICONERROR, "Warning", "No " & $install4jName & " executable found in" & @CRLF & Chr(34) & $install4jLocation & Chr(34) & "," & @CRLF & _
      "please clean up your installation(s) and program folder(s)," & @CRLF & "then run this program again - " & @ScriptName)
    Exit 1
  EndIf
  Return $install4jLocation
EndFunc ; ===> GetInstall4jLocation()

Func OpenVariant($variantName)
  WinActivate($install4jHandle)
  $sendTabSpace = StringRegExp(WinGetTitle($install4jHandle), " \[" & StringReplace($variantName, ".", "\.") & "\]$")
  Send("^o")
  WinWait("Open install4j project")
  Send($gsehenLocation & "\installer\" & $variantName & ".xml")
  Send("{ENTER}")
  $dialogHandle = WinWait("install4j")
  If $sendTabSpace Then
    Send("{TAB} ")
  Else
    Send("{ENTER}")
  EndIf
EndFunc ; ===> OpenVariant()

$install4jLocation = GetInstall4jLocation()
Run($install4jLocation & "\bin\install4j.exe")
$install4jHandle = WinWait("[REGEXPTITLE:" & $install4jWindowTitleRE & "]")
$actualWindowTitle = WinGetTitle($install4jHandle)
If StringRegExpReplace($actualWindowTitle, " \[.*\]$", "") <> $install4jWindowTitle Then
  MsgBox($MB_ICONERROR, "Warning", "The " & $install4jName & " window title" & @CRLF & Chr(34) & $actualWindowTitle & Chr(34) & @CRLF & _
    "indicates that you're not using the" & @CRLF & Chr(34) & $install4jWindowTitle & Chr(34) & "." & @CRLF & "Please use another installation," & _
    @CRLF & "then run this program again - " & @ScriptName)
EndIf
WinSetState($install4jHandle, "", @SW_MINIMIZE)

$gsehenLocation = ""
Dim $gsehenLocationsArray[0]
Do
  $gsehenLocations = AddLocationIfExists("", EnvGet("SystemDrive") & "\" & $gsehenFolderName)
  $gsehenLocations = AddLocationIfExists($gsehenLocations, EnvGet("USERPROFILE") & "\" & $gsehenFolderName)
  $gsehenLocations = AddLocationIfExists($gsehenLocations, EnvGet("USERPROFILE") & "\Documents\" & $gsehenFolderName)
  $gsehenLocationChoice = ShowLocationChoiceDialog($gsehenLocations, $gsehenFolderName & " folders", _
    "to exit and checkout" & @CRLF & "GSEHEN, then run this program again - " & @ScriptName, EnvGet("SystemDrive") & "\", $gsehenLocationsArray)
  If $gsehenLocationChoice = "" Then
    Exit
  Else
    $gsehenLocation = EvaluateChoice($gsehenLocationChoice, $gsehenLocationsArray)
  EndIf
Until $gsehenLocation <> ""

OpenVariant("windows64.install4j")
