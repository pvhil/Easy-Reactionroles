import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class main {

    public static JDA jda;
    private static String Discordtoken;

    public static void main(String[] args) throws LoginException, IOException {

        try {
            File botTXT = new File("bottoken.txt");
            if (botTXT.createNewFile()) {
                System.out.println("bottoken.txt created!! Please put your Bot Token inside it and restart the .jar!!");
                return;
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            File myObj = new File("bottoken.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Discordtoken = data;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


        jda = JDABuilder.createDefault(Discordtoken)
                .addEventListeners(new Discordclass())
                .build();

        Path myObj = Paths.get("data");
        try {
            Files.createDirectory(myObj);
        } catch (FileAlreadyExistsException ignored) {
        }


        System.out.println("Reaction Roles started!");


    }


}
