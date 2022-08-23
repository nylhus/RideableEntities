package net.nylhus.rideableentities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class RideableEntities extends JavaPlugin implements Listener {
    public ProtocolManager protocolManager;
    List<EntityType> flying = Arrays.asList(EntityType.ALLAY, EntityType.VEX, EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.BLAZE, EntityType.BAT, EntityType.BEE, EntityType.PARROT, EntityType.PHANTOM, EntityType.GHAST);
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        EntityType entityType = event.getPlayer().getVehicle().getType();
                        Entity entity = event.getPlayer().getVehicle();
                        Vector vector = event.getPlayer().getVelocity();
                        vector.setX(vector.getX() * 50);
                        vector.setZ(vector.getZ() * 50);
                        if (!flying.contains(entityType)) {
                            if (event.getPlayer().getVelocity().getY() == 0) {
                                vector.setY(-1);
                            }
                        } else {
                            vector.setY((event.getPlayer().getLocation().getPitch() * -1) / 45);
                        }

                        entity.setVelocity(vector);
                        entity.setRotation(event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch());

                    }
                }
        );
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent e) {
        EntityType clicked = e.getRightClicked().getType();
        if (e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            e.getRightClicked().addPassenger(e.getPlayer());

            if (flying.contains(clicked)) {
                e.getRightClicked().setGravity(false);
                e.getPlayer().setGravity(false);
            }

        }
    }

    @EventHandler
    public void onShoot(PlayerInteractEvent e) {
        EntityType vehicle;
        if (e.getPlayer().getVehicle() != null) {
            vehicle = e.getPlayer().getVehicle().getType();
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (vehicle.equals(EntityType.ENDER_DRAGON) || vehicle.equals(EntityType.GHAST) || vehicle.equals(EntityType.BLAZE)) {
                    e.getPlayer().launchProjectile(Fireball.class);
                } else if (vehicle.equals(EntityType.WITHER)) {
                    e.getPlayer().launchProjectile(WitherSkull.class);
                }
            }
        }
    }
    @EventHandler
    public void onDismount(VehicleExitEvent e) {
        Entity entity = e.getExited();
        entity.setGravity(true);
    }

    @EventHandler
    public void onCollide(VehicleBlockCollisionEvent e) {
        Player player = (Player) e.getVehicle().getPassengers();
        Vector vector = player.getVelocity();
        vector.setY(0.5);
    }
}