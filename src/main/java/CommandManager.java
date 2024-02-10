import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
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
import java.awt.image.ImagingOpException;
import java.io.*;
import java.util.List;
import java.util.*;

public class CommandManager extends ListenerAdapter {

    static private ArrayList<String> admin_user = new ArrayList<String>();
    static {
        admin_user.add("th1rox");
        admin_user.add("azzamac.graou");
    }

    private HashMap<String, ArrayList<Integer>> armes;
    private HashMap<String, ArrayList<Integer>> magie;
    private HashMap<String, ArrayList<Integer>> grade;
    private ArrayList<String> lootable_users;

    private final JFrame frame;

    private final ShardManager shardManager;

    private boolean loot_available = true;

    public CommandManager(JFrame frame, ShardManager shardManager) throws IOException {
        this.frame = frame;
        this.shardManager = shardManager;
        this.armes = new HashMap<String,ArrayList<Integer>>();
        this.magie = new HashMap<String,ArrayList<Integer>>();
        this.grade = new HashMap<String,ArrayList<Integer>>();
        this.lootable_users = new ArrayList<>();
        extractArmes();
        extractGrade();
        extractMagie();
        extractLootableUsers();
    }

    private void extractLootableUsers() throws  IOException{
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/LootMembers.txt"))) {
            String line;
            while((line=br.readLine())!=null){
                lootable_users.add(line);
            }
        } catch (IOException e) {
            System.out.println("ERREUR : Fichier LootMembers.txt introuvable ou non lisible");
            System.exit(-1);
        }
    }

    /**
     * Méthode d'extraction des armes dans le fichier armes.txt présent dans le chemin LootGenerator/assets/Armes/
     */
    private void extractArmes() {
        int pourcent=0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Armes.txt"))) {
            String line;
            while((line=br.readLine())!=null){
                String[] chaine = line.split(";");
                ArrayList<Integer> intervalle = new ArrayList<Integer>();
                intervalle.add(pourcent);
                intervalle.add(pourcent+=Integer.parseInt(chaine[1])-1);
                armes.put(chaine[0], intervalle);
            }
            br.close();

            if(pourcent>100){
                this.loot_available=false;
                JOptionPane.showMessageDialog(frame, "Total des pourcentages des armes > 100.\nLa commande **/loot** est indisponible.\nPour la rendre disponible, vous pouvez utiliser les commandes du bot.");
            }
            else{
                this.loot_available=true;
            }

        } catch (IOException e) {
            System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
            System.exit(-1);
        }

        for(String string : armes.keySet()){
            System.out.println(string +" -> ["+armes.get(string).get(0)+","+armes.get(string).get(1)+"]");
        }

    }

    private void extractMagie(){
        boolean continuer=false;
        int pourcent=0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Magies.txt"))) {
            String line;
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
                this.loot_available=false;
                JOptionPane.showMessageDialog(frame, "Total des pourcentages des magies > 100.\nLa commande **/loot** est indisponible.\nPour la rendre disponible, vous pouvez utiliser les commandes du bot.");
            }
            else{
                this.loot_available=true;
            }

        } catch (IOException e) {
            System.out.println("ERREUR : Fichier Magies.txt introuvable ou non lisible");
            System.exit(-1);
        }


        for(String string : magie.keySet()){
            System.out.println(string +" -> ["+magie.get(string).get(0)+","+magie.get(string).get(1)+"]");
        }

    }


    private void extractGrade(){
        boolean continuer=false;
        int pourcent=0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Grades.txt"))) {
            String line;
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
                this.loot_available=false;
                JOptionPane.showMessageDialog(frame, "Total des pourcentages des magies différent de 100.\nLa commande **/loot** est indisponible.\nPour la rendre disponible, vous pouvez utiliser les commandes du bot.");
            }
            else{
                this.loot_available=true;
            }

        } catch (IOException e) {
            System.out.println("ERREUR : Fichier Grades.txt introuvable ou non lisible");
            System.exit(-1);
        }

        for(String string : grade.keySet()){
            System.out.println(string +" -> ["+grade.get(string).get(0)+","+grade.get(string).get(1)+"] --> RGB("+grade.get(string).get(2)+","+grade.get(string).get(3)+","+grade.get(string).get(4)+")");
        }

    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        switch (command) {
            case "loot" -> {
                if (lootable_users.contains(event.getUser().getName()) && this.loot_available) {
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
                }
                else if(!lootable_users.contains(event.getUser().getName())){
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
                else{
                    event.reply("Commande /loot indisponible. Veuillez modifier les armes, les grades ou les magies pour régler le problème").queue();
                }
            }
            case "off" -> {
                if (admin_user.contains(event.getUser().getName())) {
                    event.reply("Merci d'avoir utilisé lootGenerator codé par th1roX. A bientôt soldat !").queue();

                    this.shardManager.setStatus(OnlineStatus.OFFLINE);

                    System.exit(0);
                }
                else {
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "addlootmember" -> {
                if(admin_user.contains(event.getUser().getName())){
                    String userName = Objects.requireNonNull(event.getOption("nom")).getAsString();
                    if(lootable_users.contains(userName)){
                        event.reply(userName + " peut déjà utiliser la commande /loot !").queue();
                    }
                    else{
                        try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/LootMembers.txt"))) {
                            for(String user : lootable_users){
                                br.append(user).append("\n");
                            }
                            br.append(userName).append("\n");
                        } catch (IOException e) {
                            System.out.println("ERREUR : Fichier LootMembers.txt introuvable ou non lisible");
                            System.exit(-1);
                        }

                        lootable_users = new ArrayList<>();
                        try {
                            extractLootableUsers();
                        } catch (IOException e) {
                            System.out.println("ERREUR :  Rechargement du fichier LootMembers.txt introuvable ou non lisible");
                        }

                        StringBuilder reponse = new StringBuilder("-----Utilisateurs de /loot-----\n");
                        for (String admin : lootable_users) reponse.append("- ").append(admin).append("\n");
                        event.reply(userName + " a bien été ajouté aux utilisateurs de la commande /loot !\n"+reponse.toString()).queue();
                    }
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "showlootmember" -> {
                StringBuilder reponse = new StringBuilder("-----Utilisateurs de /loot-----\n");
                for (String admin : lootable_users) reponse.append("- ").append(admin).append("\n");
                event.reply(reponse.toString()).queue();
            }
            case "showadmin" -> {
                StringBuilder reponse = new StringBuilder("-----Administrateurs du bot-----\n");
                for (String admin : admin_user) reponse.append("* ").append(admin).append("\n");
                event.reply(reponse.toString()).queue();
            }
            case "removelootmember" -> {
                if(admin_user.contains(event.getUser().getName())){
                    String userName = Objects.requireNonNull(event.getOption("nom")).getAsString();
                    if(lootable_users.contains(userName)){
                        lootable_users.remove(userName);
                        try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/LootMembers.txt"))) {
                            for(String user : lootable_users){
                                br.append(user).append("\n");
                            }
                        } catch (IOException e) {
                            System.out.println("ERREUR : Fichier LootMembers.txt introuvable ou non lisible");
                            System.exit(-1);
                        }

                        StringBuilder reponse = new StringBuilder("-----Utilisateurs de /loot-----\n");
                        for (String admin : lootable_users) reponse.append("- ").append(admin).append("\n");
                        event.reply(userName + " a bien été supprimé aux utilisateurs de la commande /loot !\n"+reponse.toString()).queue();
                    }
                    else{
                        event.reply(userName + " n'est déjà pas autorisé à utiliser la commande /loot !").queue();
                    }
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "showarmes" -> {
                StringBuilder reponse = new StringBuilder("-----Armes-----\n");
                try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Armes.txt"))) {
                    String[] line;
                    String ligne;
                    while((ligne=br.readLine())!=null){
                        line = ligne.split(";");
                        reponse.append("- ").append(line[0]).append(" -> ").append(line[1]).append("%\n");
                    }
                } catch (IOException e) {
                    System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                    System.exit(-1);
                }
                reponse.append("---------------\n");
                event.reply(reponse.toString()).queue();
            }
            case "showmagies" -> {
                StringBuilder reponse = new StringBuilder("-----Magies-----\n");
                try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Magies.txt"))) {
                    String[] line;
                    String ligne;
                    while((ligne=br.readLine())!=null){
                        line = ligne.split(";");
                        reponse.append("- ").append(line[0]).append(" -> ").append(line[1]).append("%\n");
                    }
                } catch (IOException e) {
                    System.out.println("ERREUR : Fichier Magies.txt introuvable ou non lisible");
                    System.exit(-1);
                }
                reponse.append("----------------\n");
                event.reply(reponse.toString()).queue();
            }
            case "showgrades" -> {
                StringBuilder reponse = new StringBuilder("-----Grades-----\n");
                try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/Grades.txt"))) {
                    String[] line;
                    String ligne;
                    while((ligne=br.readLine())!=null){
                        line = ligne.split(";");
                        reponse.append("- ").append(line[0]).append(" -> ").append(line[1]).append("%\n");
                    }
                } catch (IOException e) {
                    System.out.println("ERREUR : Fichier Grades.txt introuvable ou non lisible");
                    System.exit(-1);
                }
                reponse.append("----------------\n");
                event.reply(reponse.toString()).queue();
            }
            case "addarme" -> {
                if(admin_user.contains(event.getUser().getName())){
                    String ArmeName = Objects.requireNonNull(event.getOption("arme")).getAsString();
                    int ArmePct = Objects.requireNonNull(event.getOption("pourcentage")).getAsInt();

                    if(armes.containsKey(ArmeName)){
                        event.reply(ArmeName + "existe déjà dans le générateur !").queue();
                    }
                    else{
                        try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/Armes.txt"))) {
                            for(String arme : armes.keySet()){
                                br.append(arme).append(";").append(armes.get(arme).get(1)-armes.get(arme).get(0)+1+"\n");
                            }
                            br.append(ArmeName).append(";").append(ArmePct+"\n");
                        } catch (IOException e) {
                            System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                            System.exit(-1);
                        }

                        armes = new HashMap<>();
                        extractArmes();
                        event.reply(ArmeName+" a bien été ajouté au générateur !\n").queue();
                    }
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "addmagie" -> {
                if(admin_user.contains(event.getUser().getName())){
                    String MagieName = Objects.requireNonNull(event.getOption("magie")).getAsString();
                    int MagiePct = Objects.requireNonNull(event.getOption("pourcentage")).getAsInt();

                    if(magie.containsKey(MagieName)){
                        event.reply(MagieName + "existe déjà dans le générateur !").queue();
                    }
                    else{
                        try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/Magies.txt"))) {
                            for(String m : magie.keySet()){
                                br.append(m).append(";").append(magie.get(m).get(1)-magie.get(m).get(0)+1+"\n");
                            }
                            br.append(MagieName).append(";").append(MagiePct+"\n");
                        } catch (IOException e) {
                            System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                            System.exit(-1);
                        }

                        magie = new HashMap<>();
                        extractMagie();
                        event.reply(MagieName+" a bien été ajouté au générateur !\n").queue();
                    }
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "addgrade" -> {
                if(admin_user.contains(event.getUser().getName())){
                    String GradeName = Objects.requireNonNull(event.getOption("magie")).getAsString();
                    int GradePct = Objects.requireNonNull(event.getOption("pourcentage")).getAsInt();
                    int Redcolor = Objects.requireNonNull(event.getOption("rouge")).getAsInt();
                    int Greencolor = Objects.requireNonNull(event.getOption("vert")).getAsInt();
                    int Bluecolor = Objects.requireNonNull(event.getOption("bleu")).getAsInt();
                    if(Redcolor<0 || Redcolor>255){
                        event.reply("Couleur Rouge incorrecte : "+Redcolor+" - [0-255] attendu").queue();
                    }
                    else if(Greencolor<0 || Greencolor>255){
                        event.reply("Couleur Vert incorrecte : "+Greencolor+" - [0-255] attendu").queue();
                    }
                    else if(Bluecolor<0 || Bluecolor>255){
                        event.reply("Couleur Bleu incorrecte : "+Bluecolor+" - [0-255] attendu").queue();
                    }
                    else {
                        if (armes.containsKey(GradeName)) {
                            event.reply(GradeName + "existe déjà dans le générateur !").queue();
                        } else {
                            try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/Grades.txt"))) {
                                for (String g : grade.keySet()) {
                                    br.append(g).append(";").append(grade.get(g).get(1) - grade.get(g).get(0) + 1 + ";").append(grade.get(g).get(2) + ",").append(grade.get(g).get(3) + ",").append(grade.get(g).get(4) + "\n");
                                }
                                br.append(GradeName).append(";").append(GradePct + ";").append(Redcolor + ",").append(Greencolor + ",").append(Bluecolor + "\n");
                            } catch (IOException e) {
                                System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                                System.exit(-1);
                            }

                            armes = new HashMap<>();
                            extractGrade();
                            event.reply(GradeName + " a bien été ajouté au générateur !\n").queue();
                        }
                    }
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "removearme" -> {
                if(admin_user.contains(event.getUser().getName())){
                    String ArmeName = Objects.requireNonNull(event.getOption("arme")).getAsString();
                    if(armes.containsKey(ArmeName)){
                        armes.remove(ArmeName);
                        try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/Armes.txt"))) {
                            for(String arme : armes.keySet()){
                                br.append(arme).append(";").append(armes.get(arme).get(1)-armes.get(arme).get(0)+1+"\n");
                            }
                        } catch (IOException e) {
                            System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                            System.exit(-1);
                        }

                        armes = new HashMap<>();
                        extractArmes();
                        event.reply(ArmeName+" a bien été supprimé du générateur !\n").queue();
                    }
                    else{
                        event.reply(ArmeName + "n'existe pas dans le générateur !").queue();
                    }
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "removemagie" -> {
                if(admin_user.contains(event.getUser().getName())){
                    String MagieName = Objects.requireNonNull(event.getOption("magie")).getAsString();
                    if(magie.containsKey(MagieName)){
                        magie.remove(MagieName);
                        try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/Magies.txt"))) {
                            for(String m : magie.keySet()){
                                br.append(m).append(";").append(magie.get(m).get(1)-magie.get(m).get(0)+1+"\n");
                            }
                        } catch (IOException e) {
                            System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                            System.exit(-1);
                        }

                        magie = new HashMap<>();
                        extractMagie();
                        event.reply(MagieName+" a bien été supprimé du générateur !\n").queue();
                    }
                    else{
                        event.reply(MagieName + "n'existe pas dans le générateur !").queue();
                    }
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "removegrade" -> {
                if(admin_user.contains(event.getUser().getName())){
                    String GradeName = Objects.requireNonNull(event.getOption("grade")).getAsString();
                    if(magie.containsKey(GradeName)){
                        magie.remove(GradeName);
                        try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/Grades.txt"))) {
                            for (String g : grade.keySet()){
                                br.append(g).append(";").append(grade.get(g).get(1) - grade.get(g).get(0) + 1 + ";").append(grade.get(g).get(2) + ",").append(grade.get(g).get(3) + ",").append(grade.get(g).get(4) + "\n");
                            }
                        }
                        catch (IOException e) {
                            System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");

                        }

                        grade = new HashMap<>();
                        extractGrade();
                        event.reply(GradeName+" a bien été supprimé du générateur !").queue();
                    }
                    else{
                        event.reply(GradeName + "n'existe pas dans le générateur !").queue();
                    }
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "cleararmes" -> {
                if(admin_user.contains(event.getUser().getName())){
                    armes = new HashMap<>();
                    try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/Armes.txt"))) {
                        br.append("");
                    }
                    catch (IOException e) {
                        System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                    }
                    extractArmes();
                    event.reply("Les armes ont bien été réinitialisées !\n Utilisez la commande _/addarme_ pour ajouter des armes dau générateur").queue();
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "clearmagies" -> {
                if(admin_user.contains(event.getUser().getName())){
                    magie = new HashMap<>();
                    try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/Magies.txt"))) {
                        br.append("");
                    }
                    catch (IOException e) {
                        System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                    }
                    extractMagie();
                    event.reply("Les magies ont bien été réinitialisées !\n Utilisez la commande _/addmagie_ pour ajouter des magies au générateur").queue();
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
            case "cleargrades" -> {
                if(admin_user.contains(event.getUser().getName())){
                    grade = new HashMap<>();
                    try (BufferedWriter br = new BufferedWriter(new FileWriter("src/main/resources/Grades.txt"))) {
                        br.append("");
                    }
                    catch (IOException e) {
                        System.out.println("ERREUR : Fichier Armes.txt introuvable ou non lisible");
                    }
                    extractGrade();
                    event.reply("Les grades ont bien été réinitialisés !\n Utilisez la commande _/addgrade_ pour ajouter des grades d'armes au générateur").queue();
                }
                else{
                    event.reply(event.getUser().getAsMention() + ": Vous n'avez pas l'autorisation d'utiliser cette commande !").queue();
                }
            }
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event){
        List<CommandData> commandData = new ArrayList<CommandData>();

        commandData.add(Commands.slash("loot","Obtenez du loot aléatoirement."));
        commandData.add(Commands.slash("off","Eteindre le bot."));

        OptionData optionLootable = new OptionData(OptionType.STRING, "nom", "Nom du joueur", true);
        commandData.add(Commands.slash("addlootmember","Autoriser une personne à utiliser la commande /loot.").addOptions(optionLootable));
        OptionData optionNonLootable = new OptionData(OptionType.STRING, "nom", "Nom du joueur", true);
        commandData.add(Commands.slash("removelootmember","Surpprime l'autorisation à une personne d'utiliser la commande /loot.").addOptions(optionNonLootable));
        commandData.add(Commands.slash("showlootmember","Affiche les personnes autorisées à utiliser la commande /loot."));

        commandData.add(Commands.slash("showadmin","Affiche les administrateurs du bot LootGenerator."));

        commandData.add(Commands.slash("showarmes","Affiche les armes pouvant être obtenues."));
        commandData.add(Commands.slash("showmagies","Affiche les magies pouvant être obtenues sur certaines armes."));
        commandData.add(Commands.slash("showgrades","Affiche les grades pouvant être obtenues sur les armes."));

        OptionData optionNomArme = new OptionData(OptionType.STRING, "arme", "Nom de l'arme à ajouter", true);
        OptionData optionPctArme = new OptionData(OptionType.INTEGER, "pourcentage", "Pourcentage d'obtention de l'arme", true);
        commandData.add(Commands.slash("addarme","Ajouter une arme au générateur.").addOptions(optionNomArme,optionPctArme));

        OptionData optionNomMagie = new OptionData(OptionType.STRING, "magie", "Nom de la magie à ajouter", true);
        OptionData optionPctMagie = new OptionData(OptionType.INTEGER, "pourcentage", "Pourcentage d'obtention de la magie sur une arme", true);
        commandData.add(Commands.slash("addmagie","Ajouter une magie au générateur.").addOptions(optionNomMagie,optionPctMagie));

        OptionData optionNomGrade = new OptionData(OptionType.STRING, "grade", "Nom du grade à ajouter", true);
        OptionData optionPctGrade = new OptionData(OptionType.INTEGER, "pourcentage", "Pourcentage d'obtention du grade sur une arme", true);
        OptionData optionColRouge = new OptionData(OptionType.STRING, "rouge", "Nuance de rouge [0-255]", true);
        OptionData optionColVert = new OptionData(OptionType.INTEGER, "vert", "Nuance de vert [0-255]", true);
        OptionData optionColBleu = new OptionData(OptionType.STRING, "bleu", "Nuance de bleu [0-255]", true);
        commandData.add(Commands.slash("addgrade","Ajouter un grade d'arme au générateur avec une couleur de RGB.").addOptions(optionNomGrade,optionPctGrade,optionColRouge,optionColVert,optionColBleu));

        optionNomArme = new OptionData(OptionType.STRING, "arme", "Nom de l'arme à supprimer", true);
        commandData.add(Commands.slash("removearme","Supprimer une arme du générateur.").addOptions(optionNomArme));

        optionNomMagie = new OptionData(OptionType.STRING, "arme", "Nom de la magie à supprimer", true);
        commandData.add(Commands.slash("removemagie","Supprimer une magie du générateur.").addOptions(optionNomMagie));

        optionNomGrade = new OptionData(OptionType.STRING, "grade", "Nom du grade à supprimer", true);
        commandData.add(Commands.slash("removegrade","Supprimer un grade du générateur.").addOptions(optionNomGrade));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
