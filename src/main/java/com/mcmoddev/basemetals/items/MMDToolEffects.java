package com.mcmoddev.basemetals.items;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.mcmoddev.basemetals.data.AchievementNames;
import com.mcmoddev.basemetals.data.MaterialNames;
import com.mcmoddev.lib.data.Names;
import com.mcmoddev.lib.data.SharedStrings;
import com.mcmoddev.lib.init.Achievements;
import com.mcmoddev.lib.init.Materials;
import com.mcmoddev.lib.material.MMDMaterial;
import com.mcmoddev.lib.util.ConfigBase.Options;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Tool Effects
 */
public abstract class MMDToolEffects {

	private static final String TOOLTIP = "tooltip.";
	private static final String ARMOR = ".armor";
	private static final String TOOL = ".tool";

	private static final int EFFECT_DURATION = 45;

	private static final Map<EntityPlayer, AtomicInteger> starsteelUpdateCache = new HashMap<>();
	private static final Map<EntityPlayer, AtomicInteger> adamantineUpdateCache = new HashMap<>();
	private static final Map<EntityPlayer, AtomicInteger> leadUpdateCache = new HashMap<>();

	private MMDToolEffects() {
		throw new IllegalAccessError(SharedStrings.NOT_INSTANTIABLE);
	}

	/**
	 *
	 * @param material
	 *            The material
	 * @param itemStack
	 *            The item
	 * @param target
	 *            The target
	 * @param attacker
	 *            The attacker
	 */
	public static void extraEffectsOnAttack(final MMDMaterial material, final ItemStack itemStack,
			final EntityLivingBase target, final EntityLivingBase attacker) {
		final String materialName = material.getName();
		if (materialName.equals(MaterialNames.COLDIRON)) {
			if (target.isImmuneToFire()) {
				final DamageSource extraDamage = DamageSource.GENERIC;
				target.attackEntityFrom(extraDamage, 3f);
			}
		} else if (materialName.equals(MaterialNames.ADAMANTINE)) {
			if (target.getMaxHealth() > 20f) {
				final DamageSource extraDamage = DamageSource.GENERIC;
				target.attackEntityFrom(extraDamage, 4f);
			}
		} else if (materialName.equals(MaterialNames.MITHRIL)) {
			if (target.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
				final PotionEffect wither = new PotionEffect(MobEffects.WITHER, 60, 3);
				final PotionEffect blind = new PotionEffect(MobEffects.BLINDNESS, 60, 1);
				target.addPotionEffect(wither);
				target.addPotionEffect(blind);
			}
		} else if ((materialName.equals(MaterialNames.AQUARIUM)) && (target.canBreatheUnderwater())) {
			final DamageSource extraDamage = DamageSource.GENERIC;
			target.attackEntityFrom(extraDamage, 4f);
		}
	}

	/**
	 *
	 * @param material
	 *            The material
	 * @param item
	 *            The item
	 * @param world
	 *            The world
	 * @param crafter
	 *            The crafter
	 */
	public static void extraEffectsOnCrafting(final MMDMaterial material, final ItemStack item, final World world,
			final EntityPlayer crafter) {
		// TODO: do nothing for now. This would be where achievements or automatic
		// enchantments could appear
	}

	public static void extraEffectsOnArmorUpdate(final World w, final EntityPlayer player, final MMDMaterial material,
			final ItemStack armor, int i) {
		// some sanity checks
		if (armor.isEmpty())
			return;
		if (armor.getItem() == null)
			return;
		if (player == null)
			return;

		final String materialName = material.getName();
		if (i % 2 == 0) {
			// count armor pieces
			if (materialName.equals(MaterialNames.STARSTEEL)) {
				starsteel: {
					// used to count up the starsteel armor items
					countArmorPieces(starsteelUpdateCache, player);
					// Achievement
					if (Materials.hasMaterial(MaterialNames.STARSTEEL)) {
						if ((armor.getItem() == Materials.getMaterialByName(MaterialNames.STARSTEEL).getItem(Names.BOOTS)) && (Options.enableAchievements())) {
							player.addStat(Achievements.getAchievementByName(AchievementNames.MOON_BOOTS), 1);
						}
					}
					break starsteel;
				}
			}
			if (materialName.equals(MaterialNames.LEAD)) {
				lead: {
					// used to count up the Lead armor items
					countArmorPieces(leadUpdateCache, player);
					break lead;
				}
			}
			if (materialName.equals(MaterialNames.ADAMANTINE)) {
				adamantine: {
					// used to count up the Adamantine armor items
					countArmorPieces(adamantineUpdateCache, player);
					break adamantine;
				}
			}
		} else {
			// apply potion effects. Note that "Level I" is actually effect level 0 in the
			// effect constructor
			if (materialName.equals(MaterialNames.STARSTEEL)) {
				starsteel: {
					if (!starsteelUpdateCache.containsKey(player))
						break starsteel;
					final int num = starsteelUpdateCache.get(player).getAndSet(0);
					if (num == 0)
						break starsteel;
					final PotionEffect jumpBoost = new PotionEffect(MobEffects.JUMP_BOOST, EFFECT_DURATION, num - 1,
							false, false);
					player.addPotionEffect(jumpBoost);
					if (num > 1) {
						final PotionEffect speedBoost = new PotionEffect(MobEffects.SPEED, EFFECT_DURATION, num - 2,
								false, false);
						player.addPotionEffect(speedBoost);
					}
					break starsteel;
				}
			}
			if (materialName.equals(MaterialNames.LEAD)) {
				lead: {
					if (!(leadUpdateCache.containsKey(player)))
						break lead;
					final int level = leadUpdateCache.get(player).getAndSet(0) / 2;
					if (level == 0)
						break lead;
					if (level > 0) {
						final PotionEffect speedLoss = new PotionEffect(MobEffects.SLOWNESS, EFFECT_DURATION, level - 1,
								false, false);
						player.addPotionEffect(speedLoss);
					}
					break lead;
				}
			}
			if (materialName.equals(MaterialNames.ADAMANTINE)) {
				adamantine: {
					if (!(adamantineUpdateCache.containsKey(player)))
						break adamantine;
					final int num = adamantineUpdateCache.get(player).getAndSet(0);
					final int level = num / 2;
					if (level == 0)
						break adamantine;
					if (level > 0) {
						final PotionEffect protection = new PotionEffect(MobEffects.RESISTANCE, EFFECT_DURATION,
								level - 1, false, false);
						player.addPotionEffect(protection);
					}
					// Achievement
					if (Materials.hasMaterial(MaterialNames.ADAMANTINE)) {
						if ((Options.enableAchievements()) && (hasFullSuit(player, MaterialNames.ADAMANTINE))) {
							player.addStat(Achievements.getAchievementByName(AchievementNames.JUGGERNAUT), 1);
						}
					}
					break adamantine;
				}
			}
			// full suit of cold-iron makes you fire-proof
			if ((materialName.equals(MaterialNames.COLDIRON)) && (hasFullSuit(player, MaterialNames.COLDIRON))) {
				final PotionEffect fireProtection = new PotionEffect(MobEffects.FIRE_RESISTANCE, EFFECT_DURATION, 0,
						false, false);
				player.addPotionEffect(fireProtection);
				// Achievement
				if (Materials.hasMaterial(MaterialNames.COLDIRON)) {
					if ((!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == Materials.getMaterialByName(MaterialNames.COLDIRON).getItem(Names.SWORD)) && (Options.enableAchievements())) {
						player.addStat(Achievements.getAchievementByName(AchievementNames.DEMON_SLAYER), 1);
					}
				}
			}
			// full suit of Mithril protects you from withering, poison, nausea,
			// and hunger effects
			if ((materialName.equals(MaterialNames.MITHRIL)) && (hasFullSuit(player, MaterialNames.MITHRIL))) {
				final List<Potion> removeList = new LinkedList<>(); // needed to avoid concurrent modification error
				final Iterator<PotionEffect> effectIterator = player.getActivePotionEffects().iterator();
				while (effectIterator.hasNext()) {
					final PotionEffect pe = effectIterator.next();
					final Potion p = pe.getPotion();
					if (p.isBadEffect()) {
						removeList.add(p);
					}
				}
				for (final Potion p : removeList) {
					player.removePotionEffect(p);
				}
				// Achievement
				if (Materials.hasMaterial(MaterialNames.MITHRIL)) {
					if ((!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == Materials.getMaterialByName(MaterialNames.MITHRIL).getItem(Names.SWORD)) && (Options.enableAchievements())) {
						player.addStat(Achievements.getAchievementByName(AchievementNames.ANGEL_OF_DEATH), 1);
					}
				}
			}
			// full suit of Aquarium makes you breathe and heal under water
			if ((materialName.equals(MaterialNames.AQUARIUM))
					&& ((hasFullSuit(player, MaterialNames.AQUARIUM)) && (player.posY > 0) && (player.posY < 255))) {
				final Block b1 = w.getBlockState(new BlockPos(player.posX, player.posY, player.posZ)).getBlock();
				final Block b2 = w.getBlockState(new BlockPos(player.posX, player.posY + 1, player.posZ)).getBlock();
				if (b1 == Blocks.WATER && b2 == Blocks.WATER) {
					final PotionEffect waterBreathing = new PotionEffect(MobEffects.WATER_BREATHING, EFFECT_DURATION, 0,
							false, false);
					player.addPotionEffect(waterBreathing);
					final PotionEffect protection = new PotionEffect(MobEffects.RESISTANCE, EFFECT_DURATION, 0, false,
							false);
					player.addPotionEffect(protection);
					player.removePotionEffect(MobEffects.MINING_FATIGUE);
					// Achievement
					if (Materials.hasMaterial(MaterialNames.AQUARIUM)) {
						if (Options.enableAchievements()) {
							player.addStat(Achievements.getAchievementByName(AchievementNames.SCUBA_DIVER), 1);
						}
					}
				}
			}
		}
	}

	/**
	 *
	 * @param material
	 *            The material
	 * @param tooltipList
	 *            The tooltip list
	 */
	@SideOnly(Side.CLIENT)
	public static void addToolSpecialPropertiesToolTip(MMDMaterial material, java.util.List<String> tooltipList) {
		addToolSpecialPropertiesToolTip(material.getName(), tooltipList);
	}

	/**
	 *
	 * @param materialName
	 *            The materialName
	 * @param tooltipList
	 *            The tooltip list
	 */
	@SideOnly(Side.CLIENT)
	public static void addToolSpecialPropertiesToolTip(String materialName, java.util.List<String> tooltipList) {
		if (materialName.equals(MaterialNames.ADAMANTINE)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.ADAMANTINE + TOOL, 4));
		} else if (materialName.equals(MaterialNames.AQUARIUM)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.AQUARIUM + TOOL, 4));
		} else if (materialName.equals(MaterialNames.COLDIRON)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.COLDIRON + TOOL, 3));
		} else if (materialName.equals(MaterialNames.MITHRIL)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.MITHRIL + TOOL));
		} else if (materialName.equals(MaterialNames.STARSTEEL)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.STARSTEEL + TOOL, 10));
		}
	}

	/**
	 *
	 * @param material
	 *            The material
	 * @param tooltipList
	 *            The tooltip list
	 */
	@SideOnly(Side.CLIENT)
	public static void addArmorSpecialPropertiesToolTip(MMDMaterial material, java.util.List<String> tooltipList) {
		addArmorSpecialPropertiesToolTip(material.getName(), tooltipList);
	}

	/**
	 *
	 * @param materialName
	 *            The materialName
	 * @param tooltipList
	 *            The tooltip list
	 */
	@SideOnly(Side.CLIENT)
	public static void addArmorSpecialPropertiesToolTip(String materialName, java.util.List<String> tooltipList) {
		if (materialName.equals(MaterialNames.ADAMANTINE)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.ADAMANTINE + ARMOR, 4));
		} else if (materialName.equals(MaterialNames.AQUARIUM)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.AQUARIUM + ARMOR, 4));
		} else if (materialName.equals(MaterialNames.COLDIRON)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.COLDIRON + ARMOR, 3));
		} else if (materialName.equals(MaterialNames.MITHRIL)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.MITHRIL + ARMOR));
		} else if (materialName.equals(MaterialNames.STARSTEEL)) {
			tooltipList.add(I18n.format(TOOLTIP + MaterialNames.STARSTEEL + ARMOR, 10));
		}
	}

	private static void countArmorPieces(Map<EntityPlayer, AtomicInteger> updateCache, EntityPlayer player) {
		if (!(updateCache.containsKey(player))) {
			updateCache.put(player, new AtomicInteger(0));
		}

		updateCache.get(player).incrementAndGet();
	}

	private static boolean hasFullSuit(EntityPlayer player, String materialName) {
		final MMDMaterial material = Materials.getMaterialByName(materialName);

		return ((!player.inventory.armorInventory.get(3).isEmpty()) && player.inventory.armorInventory.get(3).getItem() == material.getItem(Names.HELMET)
				&& (!player.inventory.armorInventory.get(2).isEmpty()) && player.inventory.armorInventory.get(2).getItem() == material.getItem(Names.CHESTPLATE)
				&& (!player.inventory.armorInventory.get(1).isEmpty()) && player.inventory.armorInventory.get(1).getItem() == material.getItem(Names.LEGGINGS)
				&& (!player.inventory.armorInventory.get(0).isEmpty()) && player.inventory.armorInventory.get(0).getItem() == material.getItem(Names.BOOTS));
	}
}
