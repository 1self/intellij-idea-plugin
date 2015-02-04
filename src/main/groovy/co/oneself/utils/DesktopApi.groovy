package co.oneself.utils

import java.awt.Desktop

public class DesktopApi {

    def static browse(URI uri) {
        if (isDesktopAvailable()) {
            Desktop.getDesktop().browse(uri)
        } else {
            openSystemSpecific(uri.toString())
        }
    }

    private static openSystemSpecific(String uri) {

        EnumOS os = getOs();

        if (os.isLinux()) {
            if (runCommand("gnome-open " + uri)) {
                return
            }
            if (runCommand("xdg-open " + uri)) {
                return
            }
        }
        if (os.isMac()) {
            runCommand("open " + uri)
        }
        if (os.isWindows()) {
            runCommand("explorer " + uri)
        }
    }


    private static boolean isDesktopAvailable() {
        try {
            return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
        } catch (Throwable t) {
            t.printStackTrace()
            return false
        }
    }

    private static boolean runCommand(String command) {

        try {
            Process p = Runtime.getRuntime().exec(command)
            if (p == null) {
                return false
            }

            try {
                int retval = p.waitFor()
                return retval == 0
            } catch (IllegalThreadStateException e) {
                e.printStackTrace()
                return true
            }
        } catch (IOException e) {
            e.printStackTrace()
            return false
        }
    }

    public static enum EnumOS {
        linux, macos, solaris, unknown, windows

        public boolean isLinux() {
            return this == linux || this == solaris
        }

        public boolean isMac() {
            return this == macos;
        }

        public boolean isWindows() {
            return this == windows;
        }
    }


    public static EnumOS getOs() {
        String s = System.getProperty("os.name").toLowerCase()

        if (s.contains("win")) {
            return EnumOS.windows
        }
        if (s.contains("mac")) {
            return EnumOS.macos
        }
        if (s.contains("solaris")) {
            return EnumOS.solaris
        }
        if (s.contains("sunos")) {
            return EnumOS.solaris
        }
        if (s.contains("linux")) {
            return EnumOS.linux
        }
        if (s.contains("unix")) {
            return EnumOS.linux
        } else {
            return EnumOS.unknown
        }
    }
}