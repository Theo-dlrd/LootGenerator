import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    static private ArrayList<String> lootable_user = new ArrayList<String>();
    static {
        lootable_user.add("th1rox");
        lootable_user.add("azzamac.graou");
    }

    static private ArrayList<String> admin_user = new ArrayList<String>();
    static {
        admin_user.add("th1rox");
        admin_user.add("azzamac.graou");
    }

    private HashMap<String, ArrayList<Integer>> armes;
    private HashMap<String,ArrayList<Integer>> magie;
    private HashMap<String,ArrayList<Integer>> grade;

    private JFrame frame;

    private ShardManager shardManager;

    public CommandManager(JFrame frame, ShardManager shardManager) throws IOException {
        this.frame = frame;
        this.shardManager = shardManager;
        this.armes = new HashMap<String,ArrayList<Integer>>();
        this.magie = new HashMap<String,ArrayList<Integer>>();
        this.grade = new HashMap<String,ArrayList<Integer>>();
        extractArmes(frame);
        extractGrade(frame);
        extractMagie(frame);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        switch (command) {
            case "loot" -> {
                if (lootable_user.contains(event.getUser().getName())) {
                    EmbedBuilder embed = new EmbedBuilder();

                    Random rand = new Random();
                    int randomArme = rand.nextInt(100) + 1;
                    System.out.print(randomArme);
                    String arme = null;
                    for (String string : armes.keySet()) {
                        if (randomArme >= armes.get(string).get(0) && randomArme <= armes.get(string).get(1)) {
                            System.out.println(" --> [" + armes.get(string).get(0) + ", " + armes.get(string).get(1) + "]");
                            arme = string;
                            embed.setTitle(arme);
                        }
                    }

                    String gradeArme = null;
                    if (arme != null) {
                        int randomGrade = rand.nextInt(100) + 1;
                        System.out.print(randomGrade);
                        for (String string : grade.keySet()) {
                            if (randomGrade >= grade.get(string).get(0) && randomGrade <= grade.get(string).get(1)) {
                                System.out.println(" --> [" + grade.get(string).get(0) + ", " + grade.get(string).get(1) + "]");
                                gradeArme = string;
                                embed.addField("Rareté :", gradeArme, true);
                                embed.setColor(new Color(grade.get(gradeArme).get(2), grade.get(gradeArme).get(3), grade.get(gradeArme).get(4)));
                            }
                        }
                    }

                    String magieArme = null;
                    if (arme != null && gradeArme != null && !gradeArme.equals("Médiocre") && !gradeArme.equals("Commun")) {
                        int randomMagie = rand.nextInt(100) + 1;
                        System.out.print(randomMagie);
                        for (String string : magie.keySet()) {
                            if (randomMagie >= magie.get(string).get(0) && randomMagie <= magie.get(string).get(1)) {
                                System.out.println(" --> [" + magie.get(string).get(0) + ", " + magie.get(string).get(1) + "]");
                                magieArme = string;
                                embed.addField("Magie :", magieArme, true);
                            }
                        }
                    }

                    if (embed.isEmpty()) {
                        event.reply("Vous n'avez rien reçu !").queue();
                    } else {
                        event.reply("Vous avez obtenu :").queue();
                        event.getChannel().sendMessageEmbeds(embed.build()).queue();
                    }
                } else {
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "off" -> {
                if (admin_user.contains(event.getUser().getName())) {
                    event.reply("Merci d'avoir visité ma Taverne. A bientôt soldat !").queue();

                    this.shardManager.setStatus(OnlineStatus.OFFLINE);

                    System.exit(0);
                } else {
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "addLootPlayer" -> {
                for (Member m : Objects.requireNonNull(event.getGuild()).getMembers()) {
                    System.out.println(m.getUser().getName());
                }
            }
            case "removeLootPlayer" -> {

            }
            case "showArmes" -> {

            }
            case "showMagies" -> {

            }
            case "showGrades" -> {

            }
            case "addArme" -> {

            }
            case "addMagie" -> {

            }
            case "addGrade" -> {

            }
            case "removeArme" -> {

            }
            case "removeMagie" -> {

            }
            case "removeGrade" -> {

            }
        }
    }

    /**
     * Méthode d'extraction des armes dans le fichier armes.txt présent dans le chemin LootGenerator/assets/Armes/
     * @param frame [JFrame]
     */
    private void extractArmes(JFrame frame) throws IOException {
        System.out.println();
        boolean continuer=false;
        int pourcent=0;
        do{
            try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Armes.txt"))) {
                continuer=false;
                String line;
                pourcent = 0;
                while((line=br.readLine())!=null){
                    pourcent+=1;
                    String[] chaine = line.split(";");
                    ArrayList<Integer> intervalle = new ArrayList<Integer>();
                    intervalle.add(pourcent);
                    intervalle.add(pourcent+=Integer.parseInt(chaine[1])-1);
                    armes.put(chaine[0], intervalle);
                }
                br.close();

                if(pourcent>100){
                    JOptionPane.showMessageDialog(frame, "ERREUR : Total des pourcentages des armes > 100.\nModifier les pourcentages des armes du fichier Armes.txt avant de continuer");
                }
                else if(pourcent<100){
                    int choix = JOptionPane.showConfirmDialog(frame, "Total des pourcentages des armes < 100.\nSouhaitez-vous continuer ?\nPS: Avant de choisir non, pensez à modifier le fichier Armes.txt", "Avertissement", JOptionPane.YES_NO_OPTION);
                    if(choix==0){
                        continuer=true;
                    }
                }
                else{
                    continuer=true;
                }

            } catch (IOException e) {
                System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                System.exit(-1);
            }
        }while(pourcent>100 || !continuer);


        for(String string : armes.keySet()){
            System.out.println(string +" -> ["+armes.get(string).get(0)+","+armes.get(string).get(1)+"]");
        }

    }

    private void extractMagie(JFrame frame){
        boolean continuer=false;
        int pourcent=0;
        do{
            try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Magies.txt"))) {
                continuer=false;
                String line;
                pourcent = 0;
                while((line=br.readLine())!=null){
                    pourcent+=1;
                    String[] chaine = line.split(";");
                    ArrayList<Integer> intervalle = new ArrayList<Integer>();
                    intervalle.add(pourcent);
                    intervalle.add(pourcent+=Integer.parseInt(chaine[1])-1);
                    magie.put(chaine[0], intervalle);
                }
                br.close();

                if(pourcent>100){
                    JOptionPane.showMessageDialog(frame, "ERREUR : Total des pourcentages des types de magie > 100.\nModifier les pourcentages des types de magie du fichier Magies.txt avant de continuer");
                }
                else if(pourcent<100){
                    int choix = JOptionPane.showConfirmDialog(frame, "Total des pourcentages des types de magie < 100.\nSouhaitez-vous continuer ?\nPS: Avant de choisir non, pensez à modifier le fichier Magies.txt", "Avertissement", JOptionPane.YES_NO_OPTION);
                    if(choix==0){
                        continuer=true;
                    }
                }
                else{
                    continuer=true;
                }

            } catch (IOException e) {
                System.out.println("ERREUR : Fichier Magies.txt introuvable ou non lisible");
                System.exit(-1);
            }
        }while(pourcent>100 || !continuer);


        for(String string : magie.keySet()){
            System.out.println(string +" -> ["+magie.get(string).get(0)+","+magie.get(string).get(1)+"]");
        }

    }


    private void extractGrade(JFrame frame){
        boolean continuer=false;
        int pourcent=0;
        do{
            try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Grades.txt"))) {
                continuer=false;
                String line;
                pourcent = 0;
                while((line=br.readLine())!=null){
                    pourcent+=1;
                    String[] chaine = line.split(";");
                    ArrayList<Integer> infos = new ArrayList<Integer>();
                    infos.add(pourcent);
                    infos.add(pourcent+=Integer.parseInt(chaine[1])-1);
                    String[] colors = chaine[2].split(",");
                    for (String color: colors) {
                        infos.add(Integer.parseInt(color));
                    }
                    grade.put(chaine[0], infos);
                }
                br.close();

                if(pourcent!=100){
                    JOptionPane.showMessageDialog(frame, "ERREUR : Total des pourcentages des grades différent de 100.\nModifier les pourcentages des types de magie du fichier typesMagie.txt avant de continuer");
                }
                else{
                    continuer=true;
                }

            } catch (IOException e) {
                System.out.println("ERREUR : Fichier Grades.txt introuvable ou non lisible");
                System.exit(-1);
            }
        }while(pourcent!=100 || !continuer);

        for(String string : grade.keySet()){
            System.out.println(string +" -> ["+grade.get(string).get(0)+","+grade.get(string).get(1)+"] --> RGB("+grade.get(string).get(2)+","+grade.get(string).get(3)+","+grade.get(string).get(4)+")");
        }

    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event){
        List<CommandData> commandData = new ArrayList<CommandData>();

        commandData.add(Commands.slash("loot","Obtenez du loot aléatoirement."));
        commandData.add(Commands.slash("off","Eteindre le bot."));

        OptionData optionLootable = new OptionData(OptionType.STRING, "nom", "Nom du joueur", true);
        commandData.add(Commands.slash("addLootPlayer","Autoriser une personne à utiliser la commande loot.").addOptions(optionLootable));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
