/*
 * SkinsRestorer
 * Copyright (C) 2024  SkinsRestorer Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.skinsrestorer.shared.gui;

import lombok.RequiredArgsConstructor;
import net.skinsrestorer.shared.commands.library.CommandManager;
import net.skinsrestorer.shared.listeners.event.ClickEventInfo;
import net.skinsrestorer.shared.plugin.SRServerAdapter;
import net.skinsrestorer.shared.subjects.SRCommandSender;
import net.skinsrestorer.shared.subjects.SRServerPlayer;

import javax.inject.Inject;
import java.util.function.Consumer;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SharedGUI {
    public static final int HEAD_COUNT_PER_PAGE = 36;
    public static final int HEAD_COUNT_PER_PAGE_PLUS_ONE = HEAD_COUNT_PER_PAGE + 1;
    public static final String SR_PROPERTY_INTERNAL_NAME = "skinsrestorer.skull-internal-name";

    @RequiredArgsConstructor(onConstructor_ = @Inject)
    public static class ServerGUIActions implements Consumer<ClickEventInfo> {
        private final SRServerAdapter<?, ?> adapter;
        private final CommandManager<SRCommandSender> commandManager;

        @Override
        public void accept(ClickEventInfo event) {
            SRServerPlayer player = event.player();
            switch (event.material()) {
                case HEAD -> {
                    commandManager.executeCommand(player, "skin set " + event.skinName());
                    player.closeInventory();
                }
                case RED_PANE -> {
                    commandManager.executeCommand(player, "skin clear");
                    player.closeInventory();
                }
                case GREEN_PANE -> adapter.runAsync(() -> adapter.openServerGUI(player, event.currentPage() + 1));
                case YELLOW_PANE -> adapter.runAsync(() -> adapter.openServerGUI(player, event.currentPage() - 1));
            }
        }
    }

    @RequiredArgsConstructor(onConstructor_ = @Inject)
    public static class ProxyGUIActions implements Consumer<ClickEventInfo> {
        private final SRServerAdapter<?, ?> adapter;

        @Override
        public void accept(ClickEventInfo event) {
            SRServerPlayer player = event.player();
            switch (event.material()) {
                case HEAD -> {
                    adapter.runAsync(() -> event.player().sendToMessageChannel(out -> {
                        out.writeUTF("setSkin");
                        out.writeUTF(player.getName());
                        out.writeUTF(event.skinName());
                    }));
                    player.closeInventory();
                }
                case RED_PANE -> {
                    adapter.runAsync(() -> event.player().sendToMessageChannel(out -> {
                        out.writeUTF("clearSkin");
                        out.writeUTF(player.getName());
                    }));
                    player.closeInventory();
                }
                case GREEN_PANE -> adapter.runAsync(() -> event.player().requestSkinsFromProxy(event.currentPage() + 1));
                case YELLOW_PANE -> adapter.runAsync(() -> event.player().requestSkinsFromProxy(event.currentPage() - 1));
            }
        }
    }
}
