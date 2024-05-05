package ca.landonjw.gooeylibs2.api.button;

public enum FlagType
{
    /**
     * ENCHANTS only hides tool enchants, as book ones are classified as stored enchantments.
     * EXTRAS hides multiple things, according to the minecraft wiki (https://minecraft.fandom.com/wiki/Tutorials/Command_NBT_tags)
     */
    Hide_Tooltip,
    Enchantments,
    Attribute_Modifiers,
    Unbreakable,
    Can_Break,
    Can_Place_On,
    Stored_Enchantments,
    Dyed_Color,
    Trim,
    Hide_Additional_Tooltip,
    All;
}
