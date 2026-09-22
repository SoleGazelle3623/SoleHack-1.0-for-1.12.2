package com.yourwebsitespace.solehack;

import com.yourwebsitespace.solehack.modules.combat.*;
import com.yourwebsitespace.solehack.modules.exploits.*;
import com.yourwebsitespace.solehack.modules.misc.*;
import com.yourwebsitespace.solehack.modules.movement.*;
import com.yourwebsitespace.solehack.modules.render.*;
import com.yourwebsitespace.solehack.util.ConfigManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static com.yourwebsitespace.solehack.ModuleManager.register;

@Mod(modid = com.yourwebsitespace.solehack.MyMod.MODID, name = com.yourwebsitespace.solehack.MyMod.NAME, version = com.yourwebsitespace.solehack.MyMod.VERSION, clientSideOnly = true)
public class MyMod {


    public static final String MODID = "solehack";
    public static final String NAME = "SoleHack";
    public static final String VERSION = "0.1";
    public static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger("SoleHack");

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SplashScreen.show("Welcome to SoleHack ;3");


        KeyBindings.init();

        register(new SoleSuffix());
        register(new FullBright());
        register(new Strafe());
        register(new AutoWalk());
        register(new Nametag());
        register(new AutoScaffold());
        register(new AutoEZ());
        register(new ShulkerPreview());
        register(new ClearVision());
        register(new ClientHud());
        register(new HandView());
        register(new CrystalColor());
        register(new AutoMend());
        register(new Tracers());
        register(new ElytraFly());
        register(new SpeedyGonzales());
        register(new NoFall());
        register(new StorageESP());
        register(new Velocity());
        register(new CrystalAura());
        register(new FakePlayer());
        register(new Flight());
        register(new PacketFly());
        register(new WallHack());
        register(new KillAura());
        register(new Blink());
        register(new AutoTotem());
        register(new CoordExploit());
        register(new PingSpoof());
        register(new PortalGodMode());
        register(new FastPlace());
        register(new AutoPot());
        register(new Surround());
        register(new HoleESP());
        register(new AutoDupe());
        register(new XRay());
        register(new BedAura());
        register(new PacketMine());
        register(new Step());
        register(new Anchor());
        register(new Announcer());
        register(new NoSlow());
        register(new Auto32k());
        register(new NoHurtCam());
        register(new Proxy());
        register(new AutoSex());
        register(new Replenish());
        register(new QuicbotGlazer());
        register(new VisualRange());
        register(new MultiTask());
        register(new AntiBedTrap());
        register(new TotemPopVisual());
        register(new BoatFly());
        register(new HoleFill());
        register(new NoRender());
        register(new LogoutSpots());
        register(new Jesus());
        register(new AntiHunger());
        register(new ColorSigns());
        register(new PacketLimiter());
        register(new XCarry());


        SplashScreen.close();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());

        new com.yourwebsitespace.solehack.command.CommandManager();

        // 1. Load the config on startup
        com.yourwebsitespace.solehack.util.ConfigManager.loadConfig();

        // 2. Save the config automatically when the game closes
        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::saveConfig));
    }

}