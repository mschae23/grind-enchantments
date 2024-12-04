/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Grind enchantments.
 *
 * Grind enchantments is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.grindenchantments;

import java.util.List;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

public final class CodecUtils {
    private CodecUtils() {
    }

    public static <E> Codec<List<E>> listOrSingle(Codec<E> entryCodec) {
        return listOrSingle(entryCodec, entryCodec.listOf());
    }

    public static <E> Codec<List<E>> listOrSingle(Codec<E> entryCodec, Codec<List<E>> listCodec) {
        return Codec.either(listCodec, entryCodec)
            .xmap(either -> either.map(list -> list, List::of),
                list -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list));
    }
}
