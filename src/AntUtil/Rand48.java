 /*
   Copyright (C) 2021 Thomas DiModica <ricinwich@yahoo.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package AntUtil;

public final class Rand48
{

   private long seed;

   public Rand48(long seed)
   {
      this.seed = seed;
   }

   public double getNext()
   {
      seed = (25214903917L * seed + 11) & (281474976710656L - 1);
      return seed / 281474976710656.0;
   }

   public Rand48 duplicate()
   {
      return new Rand48(seed);
   }

}
