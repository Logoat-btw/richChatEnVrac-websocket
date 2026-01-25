/*
 * Copyright (C) 2025 IUT Laval - Le Mans Université
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package richChatServer.model;

import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Permet de créer manuellement des entité en précisant les auteur et les date
 * pour la génération du jeu de données.
 *
 * @author Rémi Venant
 */
@Profile("sample-data && development")
@Component
public class SampleDataForgeUtility {

    public Room withOwner(Room r, Member owner) {
        r.setOwner(owner);
        return r;
    }

    public RoomMessage withAuthor(RoomMessage msg, Member author) {
        msg.setAuthor(author);
        return msg;
    }
}
