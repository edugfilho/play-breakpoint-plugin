import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.ui.breakpoints.Breakpoint;
import com.intellij.debugger.ui.breakpoints.BreakpointManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

public class PlaceBreakpointOnLine extends AnAction {

    private static Map<String, Breakpoint> activeBreakpointsOnSource = new HashMap<>();
    private static Map<String, Breakpoint> activeBreakpointsOnTarget = new HashMap<>();


    private String makeKey(String fileName, int lineNumber) {
        return fileName + ":" + lineNumber;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor currentEditor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();
        if (currentEditor != null) {
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentEditor.getDocument());
            if (currentFile.getName().endsWith(".scala.html")) {
                toggleBreakpointOnTemplate(e, currentEditor, currentFile);
            }
        }
    }

    private void toggleBreakpointOnTemplate(AnActionEvent e, Editor currentEditor, VirtualFile currentFile) {
        String fileName = currentFile.getName();
        final String targetName = fileName.replace(".scala.html", ".template.scala.html");
        final VirtualFile targetFile = e.getProject().getBaseDir().findChild("target").findChild("templates").findChild(targetName);
        final Document targetDoc = FileDocumentManager.getInstance().getDocument(targetFile);
        final int lineInFile = getCurrentLineInFile(currentEditor);
        final int lineInTarget = findLineInTarget(lineInFile, targetDoc);
        BreakpointManager breakPointManager = DebuggerManagerEx.getInstanceEx(e.getProject()).getBreakpointManager();
        Breakpoint breakpointOnTarget = activeBreakpointsOnTarget.remove(makeKey(targetName, lineInTarget));

        if (breakpointOnTarget != null) {
            Breakpoint breakpointOnSource = activeBreakpointsOnSource.remove(makeKey(fileName, lineInFile));

            breakPointManager.removeBreakpoint(breakpointOnTarget);
            breakPointManager.removeBreakpoint(breakpointOnSource);
        } else {
            Document currentDoc = FileDocumentManager.getInstance().getDocument(currentFile);

            Breakpoint addedBkpTarget = breakPointManager.addLineBreakpoint(targetDoc, lineInTarget);
            Breakpoint addedBkpSource = breakPointManager.addLineBreakpoint(currentDoc, lineInFile);

            activeBreakpointsOnSource.put(makeKey(fileName, lineInFile), addedBkpSource);
            activeBreakpointsOnTarget.put(makeKey(targetName, lineInTarget), addedBkpTarget);
        }
    }

    private int getCurrentLineInFile(Editor currentEditor) {
        return currentEditor.getCaretModel().getPrimaryCaret().getLogicalPosition().line + 1;
    }

    private int findLineInTarget(int lineInFile, Document targetDoc) {
        final String[] lines = targetDoc.getText().split("\n");
        final String originalLineTag = "/*" + lineInFile + ".";
        int lineCount = 0;
        for (String line : lines) {
            if (line.contains(originalLineTag)) {
                return lineCount;
            }
            lineCount++;
        }
        return -1;
    }
}
