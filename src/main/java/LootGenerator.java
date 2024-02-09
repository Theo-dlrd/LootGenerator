import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.swing.*;
import java.io.IOException;

public class LootGenerator extends JFrame {

    private final ShardManager shardManager;

    public LootGenerator() throws IOException {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Dotenv config = Dotenv.configure().load();
        String token = config.get("TOKEN");

        if(token==null){
            System.out.println("ERREUR : token inconnu !");
            System.exit(-1);
        }

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("D&D"));
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS);
        this.shardManager = builder.build();

        this.shardManager.addEventListener(new CommandManager(frame, this.shardManager));
    }

    public static void main(String[] args) {
        try {
            new LootGenerator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
