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

public final class NonSplittableRandom
{

   private long seed;

   public NonSplittableRandom(long seed)
   {
       this.seed = seed;
   }

   public long getNext()
   {
      seed += 0x9e3779b97f4a7c15L;
      long z = seed;
      z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
      z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
      return z ^ (z >>> 31);
   }

   public NonSplittableRandom duplicate()
   {
      return new NonSplittableRandom(seed);
   }

}
