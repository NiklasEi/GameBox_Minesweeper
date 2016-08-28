package me.nikl.minesweeper.nms;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;

public class Update_1_8_R3 implements Update{

	@Override
	public void updateTitle(Player player, String newTitle) {
		EntityPlayer ep = ((CraftPlayer)player).getHandle();
		PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(ep.activeContainer.windowId, "minecraft:chest", new ChatMessage(newTitle), player.getOpenInventory().getTopInventory().getSize());
		ep.playerConnection.sendPacket(packet);
		ep.updateInventory(ep.activeContainer);
	}

}
