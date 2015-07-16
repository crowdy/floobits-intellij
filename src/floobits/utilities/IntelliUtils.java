package floobits.utilities;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.VFileProperty;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import floobits.FloobitsPlugin;
import floobits.common.Constants;
import floobits.common.PersistentJson;
import floobits.common.Settings;
import floobits.common.Utils;
import floobits.common.interfaces.IContext;
import floobits.common.interfaces.IFile;
import floobits.impl.FileImpl;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;


public class IntelliUtils {
    public static ArrayList<String> getAllNestedFilePaths(VirtualFile vFile) {
        final ArrayList<String> filePaths = new ArrayList<String>();
        if (!vFile.isDirectory()) {
            filePaths.add(vFile.getPath());
            return filePaths;
        }
        VfsUtil.iterateChildrenRecursively(vFile, null, new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile file) {
                if (!file.isDirectory()) {
                    filePaths.add(file.getPath());
                }
                return true;
            }
        });
        return filePaths;
    }

    public static ArrayList<IFile> getAllValidNestedFiles(final IContext context, VirtualFile vFile) {
        final ArrayList<IFile> virtualFiles = new ArrayList<IFile>();
        FileImpl fileImpl = new FileImpl(vFile);

        if (!fileImpl.isDirectory()) {
            if (fileImpl.isValid() && !context.isIgnored(fileImpl)) virtualFiles.add(fileImpl);
            return virtualFiles;
        }

        VfsUtil.iterateChildrenRecursively(vFile, null, new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile file) {
                FileImpl fileImpl = new FileImpl(file);
                if (!context.isIgnored(fileImpl) && !fileImpl.isDirectory() && fileImpl.isValid()) {
                    virtualFiles.add(fileImpl);
                }
                return true;
            }
        });
        return virtualFiles;
    }

    public static Boolean isSharable(VirtualFile virtualFile) {
        return (virtualFile != null  && virtualFile.isValid() && virtualFile.isInLocalFileSystem() && !virtualFile.is(VFileProperty.SPECIAL) && !virtualFile.is(VFileProperty.SYMLINK));
    }

    public static Boolean isAutoGenerated() {
        PersistentJson p = PersistentJson.getInstance();
        return p.auto_generated_account;
    }

    public static String getCompleteSignUpURL(Project project) {
        if (project == null) {
            return null;
        }
        if (!Settings.canFloobits()) {
            Flog.errorMessage("Error, no account details detected. You will have to sign up manually.", project);
            return null;
        }
        if(!Desktop.isDesktopSupported()) {
            Flog.errorMessage("Can't use a browser on this system.", project);
            return null;
        }
        HashMap<String, HashMap<String, String>> auth = null;
        try {
            auth = Settings.get().auth;
        } catch (Throwable e1) {
            Flog.errorMessage("Invalid JSON in ~/.floorc.json", project);
            return null;
        }

        if (auth.size() < 1) {
            Flog.error("No auth.");
            return null;
        }
        String host;
        if (auth.size() > 1) {
            host = Constants.floobitsDomain;
        } else {
            host = (String) auth.keySet().toArray()[0];
        }
        HashMap<String, String> hostAuth = auth.get(host);

        if (hostAuth == null) {
            Flog.error("This probably shouldn't happen, but there is no auth.");
            return null;
        }
        String username = hostAuth.get("username");
        if (username == null) {
            Flog.error("This probably shouldn't happen, but there is no username.");
            return null;
        }
        String secret = hostAuth.get("secret");
        return String.format("https://%s/%s/pinocchio/%s", host, username, secret);
    }


    public static void handleHyperLink(HyperlinkEvent event, Project project) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URI uri;
            try {
                uri = event.getURL().toURI();
            } catch (URISyntaxException error) {
                Flog.error(error);
                return;
            }
            FloobitsPlugin plugin = FloobitsPlugin.getInstance(project);
            if (!openInBrowser(uri, "Click to continue.", plugin.context)) {
                plugin.context.errorMessage(
                        String.format("You cannot click on links in IntelliJ apparently, try copy and paste: %s.", uri.toString()));
            }
        }
    }

    /**
     * Wrap the common Utils.openInBrowser function so that we first attempt to use
     * IntelliJ's native open-in-browser feature, which works more reliably across platforms.
     * @param uri - The link to open
     * @param defaultLinkText - Link text for hyperlink dropped into console if opening browser fails
     * @param context - Application context so that we can write to console if needed
     * @return boolean true if the browser was successfully opened.
     */
    static public boolean openInBrowser(URI uri, String defaultLinkText, IContext context) {
        boolean shown = false;
        try {
            BrowserUtil.browse(uri);
            context.statusMessage(Utils.getLinkHTML(defaultLinkText, uri.toString()));
            shown = true;
        } catch (Exception e) {
            shown = Utils.openInBrowser(uri, defaultLinkText, context);
        }
        return shown;
    }
}
