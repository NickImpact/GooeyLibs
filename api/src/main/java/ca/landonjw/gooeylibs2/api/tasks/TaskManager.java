/*
 * GooeyLibs
 * Copyright (C) 201x - 2024 landonjw
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package ca.landonjw.gooeylibs2.api.tasks;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TaskManager {

    private static TaskManager INSTANCE;
    private List<Task> tasks = new CopyOnWriteArrayList<>();

    void register(Task task) {
        if (task == null) return;
        this.tasks.add(task);
    }

    public void tick() {
        this.tasks.removeIf(task -> {
            if (task == null) return true;
            task.tick();
            return task.isExpired();
        });
    }

    public static TaskManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TaskManager();
        }
        return INSTANCE;
    }

}
