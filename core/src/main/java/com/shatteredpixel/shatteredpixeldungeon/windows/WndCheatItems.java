package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

//Cheat window: first a list of item categories, then a list of items in the chosen category
public class WndCheatItems extends Window {

	private static final int WIDTH    = 120;
	private static final int BTN_H    = 18;
	private static final int GAP      = 2;

	private static boolean uk(){
		return Messages.lang() == Languages.UKRANIAN;
	}

	public WndCheatItems() {
		this(null);
	}

	public WndCheatItems( final Catalog catalog ) {
		super();

		String titleText;
		if (catalog == null) {
			titleText = uk() ? "Видати предмети" : "Give items";
		} else {
			titleText = catalog.title();
		}

		RenderedTextBlock title = PixelScene.renderTextBlock(titleText, 9);
		title.hardlight(TITLE_COLOR);
		title.maxWidth(WIDTH - GAP * 2);
		title.setPos(GAP, GAP);
		add(title);

		float top = title.bottom() + GAP * 2;

		Component list = new Component();
		ScrollPane pane = new ScrollPane(list);
		add(pane);

		float y = 0;

		if (catalog == null) {
			ArrayList<Catalog> cats = new ArrayList<>();
			cats.addAll(Catalog.equipmentCatalogs);
			cats.addAll(Catalog.consumableCatalogs);

			for (final Catalog cat : cats) {
				if (!hasItems(cat)) continue;
				RedButton btn = new RedButton(cat.title(), 8) {
					@Override
					protected void onClick() {
						hide();
						Window w = new WndCheatItems(cat);
						com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(w);
					}
				};
				btn.setRect(0, y, WIDTH, BTN_H);
				list.add(btn);
				y += BTN_H + GAP;
			}
		} else {
			for (final Class<?> cls : catalog.items()) {
				if (!Item.class.isAssignableFrom(cls)) continue;
				final Item sample = (Item) Reflection.newInstance(cls);
				if (sample == null) continue;

				RedButton btn = new RedButton(sample.trueName(), 8) {
					@Override
					protected void onClick() {
						give(cls);
					}
				};
				btn.setRect(0, y, WIDTH, BTN_H);
				list.add(btn);
				y += BTN_H + GAP;
			}
		}

		list.setSize(WIDTH, y);

		float maxH = PixelScene.uiCamera.height - 30;
		float paneH = Math.min(y, maxH - top);
		pane.setRect(0, top, WIDTH, paneH);

		resize(WIDTH, (int) (top + paneH));
	}

	private static boolean hasItems( Catalog cat ) {
		for (Class<?> cls : cat.items()) {
			if (Item.class.isAssignableFrom(cls)) return true;
		}
		return false;
	}

	private static void give( Class<?> cls ) {
		if (Dungeon.hero == null) return;
		Item item = (Item) Reflection.newInstance(cls);
		if (item == null) return;

		item.identify();
		if (item.stackable) {
			item.quantity(10);
		}

		String name = item.name();
		if (!item.collect()) {
			Dungeon.level.drop(item, Dungeon.hero.pos).sprite.drop();
		}
		GLog.p("+ " + name);
	}
}
