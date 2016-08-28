package biz.wolter.minecraft.bukkit.event;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Door;
import org.bukkit.material.Openable;
import org.bukkit.material.PressureSensor;
import org.bukkit.material.Redstone;

import com.google.gson.Gson;

import biz.wolter.minecraft.bukkit.SmartHome;
import biz.wolter.minecraft.bukkit.rest.EventsBroadcasterResource;
import biz.wolter.minecraft.bukkit.rest.MessageType;
import biz.wolter.minecraft.bukkit.thing.Thing;
import biz.wolter.minecraft.bukkit.thing.ThingCommand;
import biz.wolter.minecraft.bukkit.thing.ThingComponent;
import biz.wolter.minecraft.bukkit.thing.ThingComponentType;
import biz.wolter.minecraft.bukkit.thing.ThingList;
import biz.wolter.minecraft.bukkit.thing.ThingLocation;
import biz.wolter.minecraft.bukkit.thing.ThingType;

public class BlockChangeListener implements Listener {

	private final Logger logger = SmartHome.getPlugin(SmartHome.class).getLogger();
	private final SmartHome smartHome = (SmartHome) SmartHome.getPlugin(SmartHome.class);

	/*
	 * Event handler for player join events.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		ThingCommand command = new ThingCommand();
		command.location = null;
		command.component = new ThingComponent();
		command.component.type = ThingComponentType.PLAYER;
		command.component.state = event.getPlayer().getName();
		Gson gson = new Gson();
		EventsBroadcasterResource.broadcastMessage(MessageType.PLAYER_JOINED, gson.toJson(command));
	}

	/*
	 * Event handler for payer leave events.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		ThingCommand command = new ThingCommand();
		command.location = null;
		command.component = new ThingComponent();
		command.component.type = ThingComponentType.PLAYER;
		command.component.state = event.getPlayer().getName();
		Gson gson = new Gson();
		EventsBroadcasterResource.broadcastMessage(MessageType.PLAYER_QUITTED, gson.toJson(command));
	}

	/*
	 * Event handler for redstone related block events.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event) {

		Block block = event.getBlock();

		ThingList thingList = smartHome.getThingList();
		ThingLocation location = new ThingLocation(block.getLocation());
		Thing thing = thingList.findThingByLocation(location);
		ThingCommand command = new ThingCommand();
		command.location = location;

		// Ignore unknown things
		if (thing == null) {
			return;
		}		
		
		// Make sure to ignore wires (instanceof RedstoneWire). Currently not
		// needed, because only relevant things will be handled (based in the
		// ThingList).
		if (block.getState().getData() instanceof Redstone) {
			Redstone redstone = (Redstone) block.getState().getData();
			// we still have the old state therefore it must be negated
			boolean isPowered = !redstone.isPowered();
			command.component = thing.getComponentByType(ThingComponentType.POWERED);
			command.component.state = isPowered;
			Gson gson = new Gson();
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));
		}

		if (block.getState().getData() instanceof PressureSensor) {
			PressureSensor pressureSensor = (PressureSensor) block.getState().getData();
			// we still have the old state therefore it must be negated
			boolean isPressed = !pressureSensor.isPressed();
			command.component = thing.getComponentByType(ThingComponentType.PRESSED);
			command.component.state = isPressed;
			Gson gson = new Gson();
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));
		}

		if ((block.getState().getData() instanceof Openable)) {
			Openable openable = (Openable) block.getState().getData();
			// we still have the old state therefore it must be negated
			boolean isOpen = !openable.isOpen();
			logger.info("BlockRedstoneEvent - Openable.isOpen " + isOpen);
			command.component = thing.getComponentByType(ThingComponentType.OPEN);
			command.component.state = isOpen;
			Gson gson = new Gson();
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));
		}

		if (block.getType().equals(Material.DAYLIGHT_DETECTOR) || block.getType().equals(Material.DAYLIGHT_DETECTOR_INVERTED)) {

			Gson gson = new Gson();

			int power = event.getNewCurrent();
			command.component = thing.getComponentByType(ThingComponentType.POWER);
			command.component.state = power;
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));

			// Some addition data (not exactly correct as these might occur
			// independently from the POWER/LIGHT)
			command.component = thing.getComponentByType(ThingComponentType.LIGHT);
			command.component.state = block.getLightLevel();
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));
			command.component = thing.getComponentByType(ThingComponentType.HUMIDITY);
			command.component.state = block.getHumidity();
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));
			command.component = thing.getComponentByType(ThingComponentType.TEMPERATURE);
			command.component.state = block.getTemperature();
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));

		}

		if (block.getType().equals(Material.REDSTONE_LAMP_OFF)) {
			command.component = thing.getComponentByType(ThingComponentType.POWERED);
			command.component.state = true;
			Gson gson = new Gson();
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));
		} else if (event.getBlock().getType().equals(Material.REDSTONE_LAMP_ON)) {
			command.component = thing.getComponentByType(ThingComponentType.POWERED);
			command.component.state = false;
			Gson gson = new Gson();
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));
		}

	}

	/*
	 * Event handler for player interaction events.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		// Please note, Redstone related interactions will be processed though
		// the BlockRedstoneEvent (@see onBlockRedstone)
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Block block = event.getClickedBlock();

		// Make sure to get the essential lower block of a two block high door
		if (block.getState().getData() instanceof Door && ((Door) block.getState().getData()).isTopHalf()) {
			block = block.getRelative(BlockFace.DOWN);
		}

		ThingList thingList = smartHome.getThingList();
		ThingLocation location = new ThingLocation(block.getLocation());
		Thing thing = thingList.findThingByLocation(location);

		// Ignore unknown things
		if (thing == null) {
			return;
		}
		
		// Check for special cases where doors can't be opened by the user
		if ((block.getState().getData() instanceof Openable) & !(block.getType().equals(Material.IRON_TRAPDOOR)
				|| block.getType().equals(Material.IRON_DOOR_BLOCK))) {
			
			Openable openable = (Openable) block.getState().getData();
			// we still have the old state therefore it must be negated
			boolean isOpen = !openable.isOpen(); 

			ThingCommand command = new ThingCommand();
			command.location = location;			 
			command.component = thing.getComponentByType(ThingComponentType.OPEN);
			command.component.state = isOpen;
			Gson gson = new Gson();
			EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(command));
		}

	}

	@EventHandler
	public void onBlockChange(EntityChangeBlockEvent event) {
		// TODO ignoring Entities so far
		// logger.info("EntityChangeBlockEvent - " + event.getBlock().getType().toString());
	}

	/*
	 * Event handler for block placement events.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {

		Block block = event.getBlock();

		switch (block.getType()) {
		// Levers (Class Lever, interface Redstone), switches and buttons (Class Button, interface Redstone)
		case LEVER:
			addThing(block, ThingType.SWITCH);
			break;
		case STONE_BUTTON:
		case WOOD_BUTTON:
			addThing(block, ThingType.BUTTON);
			break;
		// Doors (Class Door, interface Openable)
		case ACACIA_DOOR:
		case BIRCH_DOOR:
		case DARK_OAK_DOOR:
		case JUNGLE_DOOR:
		case SPRUCE_DOOR:
		case WOODEN_DOOR:
			addThing(block, ThingType.DOOR);
			break;
			// Doors players can't open directly (Class Door, interface Openable)
		case IRON_DOOR_BLOCK:
			addThing(block, ThingType.DOOR);
			break;
		// Trapdoors (class TrapDoor, interface Openable)
		case TRAP_DOOR:
			addThing(block, ThingType.DOOR);
			break;
		// Trapdoors players can't open directly (class TrapDoor, interface Openable)
		case IRON_TRAPDOOR:
			addThing(block, ThingType.DOOR);
			break;
		// Fence gates (class Gate, interface Openable)
		case FENCE_GATE:
		case SPRUCE_FENCE_GATE:
		case BIRCH_FENCE_GATE:
		case JUNGLE_FENCE_GATE:
		case DARK_OAK_FENCE_GATE:
		case ACACIA_FENCE_GATE:
			addThing(block, ThingType.DOOR);
			break;
		// Pressure plates (class PressurePlate, interface PressureSensor)
		case STONE_PLATE:
		case WOOD_PLATE:
		case GOLD_PLATE:
		case IRON_PLATE:
			addThing(block, ThingType.PRESSURE_SENSOR);
			break;
		// Tripwire (class TripwireHook, interface Redstone)
		case TRIPWIRE_HOOK:
			addThing(block, ThingType.TRIPWIRE);
			break;
		// Detectors (no dedicated class)
		case DAYLIGHT_DETECTOR:
		case DAYLIGHT_DETECTOR_INVERTED:
			addThing(block, ThingType.WEATHER_SENSOR);
			break;
		// Lamp (no dedicated class) - looks like it's always REDSTONE_LAMP_OFF at the beginning
		case REDSTONE_LAMP_OFF:
			addThing(block, ThingType. LAMP);
			break;
		default:
			// unsupported block
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		removeThing(block);
	}

	private Thing addThing(Block block, ThingType type) {

		ThingList thingList = smartHome.getThingList();

		Thing thing = new Thing();
		thing.location = new ThingLocation(block.getLocation());
		thing.type = type;
		thing.material = block.getType().name();
		
		thing.components = new ArrayList<ThingComponent>();

		if (block.getState().getData() instanceof Redstone) {
			ThingComponent component = new ThingComponent();
			component.type = ThingComponentType.POWERED;
			component.state = ((Redstone) block.getState().getData()).isPowered();
			thing.components.add(component);
		}

		if (block.getState().getData() instanceof PressureSensor) {
			ThingComponent component = new ThingComponent();
			component.type = ThingComponentType.PRESSED;
			component.state = ((PressureSensor) block.getState().getData()).isPressed();
			thing.components.add(component);
		}
		
		if (block.getState().getData() instanceof Openable) {
			ThingComponent component = new ThingComponent();
			component.type = ThingComponentType.OPEN;
			component.state = ((Openable) block.getState().getData()).isOpen();
			thing.components.add(component);
		} else if (block.getType() == Material.IRON_DOOR || block.getType() == Material.WOOD_DOOR) {
			// Doors players can't open directly
			ThingComponent component = new ThingComponent();
			component.type = ThingComponentType.OPEN;
			component.state = ((Door) block.getState().getData()).isOpen();
			thing.components.add(component);
		}

		// Read only blocks
		if (block.getType() == Material.DAYLIGHT_DETECTOR || block.getType() == Material.DAYLIGHT_DETECTOR_INVERTED) {
			ThingComponent component;
			component = new ThingComponent();
			component.type = ThingComponentType.POWER;
			component.state = block.getBlockPower();
			thing.components.add(component);

			// Some addition data
			component = new ThingComponent();
			component.type = ThingComponentType.LIGHT;
			component.state = block.getLightLevel();
			thing.components.add(component);
			component = new ThingComponent();
			component.type = ThingComponentType.HUMIDITY;
			component.state = block.getHumidity();
			thing.components.add(component);
			component = new ThingComponent();
			component.type = ThingComponentType.TEMPERATURE;
			component.state = block.getTemperature();
			thing.components.add(component);

		}

		// Read only blocks (placement always is done via LAMP_OFF)
		if (block.getType() == Material.REDSTONE_LAMP_OFF) {
			ThingComponent component;
			component = new ThingComponent();
			component.type = ThingComponentType.POWERED;
			component.state = false;
			thing.components.add(component);
		}

		thingList.add(thing);

		Gson gson = new Gson();
		// Broadcast thing including components
		EventsBroadcasterResource.broadcastMessage(MessageType.ADD_THING, gson.toJson(thing));

		return thing;

	}

	private void removeThing(Block block) {

		ThingList thingList = smartHome.getThingList();
		ThingLocation location = new ThingLocation(block.getLocation());
		Thing thing = thingList.findThingByLocation(location);

		if (thing != null) {
			thingList.remove(thing);
			Gson gson = new Gson();
			EventsBroadcasterResource.broadcastMessage(MessageType.REMOVE_THING, gson.toJson(thing));
		} else {
			// Thing not found
		}

	}

}