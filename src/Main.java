import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    public static StringBuilder log = new StringBuilder("Начало логирования\n");

    public static void main(String[] args) {
        String rootFolder = System.getProperty("user.dir");
        String parent = "./Games";
        File dir = new File(parent);
        if (!dir.exists()) {
            dir.mkdir();
            String[] folders = {
                    "src"
                    , "res"
                    , "savegames"
                    , "temp"
                    , "src/main"
                    , "src/test"
                    , "res/drawables"
                    , "res/vectors"
                    , "res/icons"
            };
            for (String folder : folders) {
                dir = new File(parent + "/" + folder);
                if (dir.mkdir()) {
                    log.append("Папка " + folder + " создана\n");
                } else {
                    if (dir.exists()) {
                        log.append("Папка " + folder + " уже создана\n");
                    } else {
                        log.append("Папка ").append(folder).append(" не создана\n");
                    }
                }
            }
            parent += "/src/main";
            String[] files = {
                    "Main.java"
                    , "Utils.java"
            };

            for (String file : files) {
                dir = new File(parent, file);

                try {
                    if (dir.createNewFile()) {
                        log.append("Файл ").append(file).append(" создан\n");
                    } else {
                        if (dir.exists()) {
                            log.append("Файл ").append(file).append(" уже создан\n");
                        } else {
                            log.append("Файл ").append(file).append(" не создан\n");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка при создании файла");
                    throw new RuntimeException(e);
                }

                try (FileWriter writer = new FileWriter("./Games/temp/temp.txt", false)) {
                    writer.write(log.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            GameProgress[] gameProgress = {
                    new GameProgress(1, 2, 3, 4.5)
                    , new GameProgress(2, 3, 4, 5.6)
                    , new GameProgress(3, 4, 5, 6.7)

            };

            ArrayList<String> stFile = new ArrayList<>();

            for (int i = 0; i < gameProgress.length; i++) {
                stFile.add(i, rootFolder + "/Games/savegames/gp_" + i + ".dat");
                saveGame(stFile.get(i), gameProgress[i]);
            }
            zipFiles(rootFolder + "/Games/savegames/zip.zip", stFile);

            delNoZip(stFile);

            openZip(rootFolder + "/Games/savegames/zip.zip", rootFolder + "/Games/savegames");

            GameProgress g1 = openProgress(rootFolder + "/Games/savegames/gp_1.dat");
            System.out.println(g1);
        } else {
            System.out.println("Католог " + parent + " уже существует");
        }
    }

    private static GameProgress openProgress(String path) {
        try (FileInputStream fis = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (GameProgress) ois.readObject();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private static boolean openZip(String patchZip, String patch) {
        try (ZipInputStream zin = new ZipInputStream(new
                FileInputStream(patchZip))) {
            ZipEntry entry;
            String name;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                FileOutputStream fout = new FileOutputStream(patch + "/" + name);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                fout.flush();
                zin.closeEntry();
                fout.close();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    private static boolean delNoZip(ArrayList stFile) {
        File file = null;
        for (int i = 0; i < stFile.size(); i++) {
            file = new File(stFile.get(i).toString());
            if (file.exists()) {
                file.delete();
            }
        }
        return true;
    }

    private static boolean zipFiles(String s, ArrayList stFile) {
        try (ZipOutputStream zout = new ZipOutputStream(new
                FileOutputStream(s));) {
            Path path = null;
            for (int i = 0; i < stFile.size(); i++) {
                path = Paths.get(stFile.get(i).toString());
                try (FileInputStream fis = new FileInputStream(stFile.get(i).toString())) {
                    ZipEntry entry = new ZipEntry(path.getFileName().toString());
                    zout.putNextEntry(entry);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    zout.write(buffer);
                    zout.closeEntry();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
            return true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    private static boolean saveGame(String addr, GameProgress gameProgress) {
        try (
                FileOutputStream fos = new FileOutputStream(addr);
                ObjectOutputStream oos = new ObjectOutputStream(fos)

        ) {
            oos.writeObject(gameProgress);
            return true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }
}