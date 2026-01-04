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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public final class TaskManager {

    private static TaskManager INSTANCE;
    private List<Task> tasks = new ArrayList<>();
    private final Queue<Task> pending = new ConcurrentLinkedQueue<>();

    void register(Task task) {
        if (task == null) return;
        this.pending.add(task);
    }

    public void tick() {
        Task newTask;
        while ((newTask = pending.poll()) != null) {
            tasks.add(newTask);
        }

        var iterator = tasks.iterator();
        while (iterator.hasNext()) {
            var task = iterator.next();
            if (task == null) {
                iterator.remove();
                continue;
            }

            task.tick();

            if (task.isExpired()) {
                iterator.remove();
            }
        }
    }

    public static TaskManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TaskManager();
        }
        return INSTANCE;
    }

}
