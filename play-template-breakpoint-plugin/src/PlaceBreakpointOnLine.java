import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.ui.breakpoints.Breakpoint;
import com.intellij.debugger.ui.breakpoints.BreakpointManager;
import com.intellij.debugger.ui.breakpoints.LineBreakpoint;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eduardo on 2017-07-08.
 */
public class PlaceBreakpointOnLine extends AnAction {

    private static Map<String, Breakpoint> activeBreakpoints = new HashMap<>();


    private String makeKey(String fileName, int lineNumber) {
        return fileName + ":" + lineNumber;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor currentEditor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();
        if (currentEditor != null) {
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentEditor.getDocument());
            String fileName = currentFile.getName();
            if (fileName.endsWith(".scala.html")) {
                String targetName = fileName.replace(".scala.html", ".template.scala.html");
                VirtualFile targetFile = e.getProject().getBaseDir().findChild("target").findChild("templates").findChild(targetName);

                int lineInFile = currentEditor.getCaretModel().getPrimaryCaret().getLogicalPosition().line + 1;
                Document targetDoc = FileDocumentManager.getInstance().getDocument(targetFile);
                //Document currentDoc = FileDocumentManager.getInstance().getDocument(currentFile);
                BreakpointManager breakPointManager = DebuggerManagerEx.getInstanceEx(e.getProject()).getBreakpointManager();
                int lineInTarget = findLineInTarget(lineInFile, targetDoc);
                Breakpoint bkp = activeBreakpoints.remove(makeKey(targetName, lineInTarget));
                if (bkp != null) {
                    breakPointManager.removeBreakpoint(bkp);
                } else {
                    Breakpoint addedBkp = breakPointManager.addLineBreakpoint(targetDoc, lineInTarget);
                    activeBreakpoints.put(makeKey(targetName, lineInTarget), addedBkp);
                    //breakPointManager.addLineBreakpoint(currentDoc, lineInFile);
                }

                System.out.print("work is done");
            }


        }
    }

    private int findLineInTarget(int lineInFile, Document targetDoc) {
        return 7;
    }
}
