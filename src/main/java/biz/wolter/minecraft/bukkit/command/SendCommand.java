package biz.wolter.minecraft.bukkit.command;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCommand implements CommandExecutor {

	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO Auto-generated method stub
		
		int x, y, z;		
		BlockState state;	
		// ((Player) sender).getWorld().getBlockAt(x, y, z)
		
        Bukkit.broadcastMessage("onCommand " + args[0]);        
        Bukkit.getLogger().info("onCommand " + args[0]);		
		
		return false;
	}

}
