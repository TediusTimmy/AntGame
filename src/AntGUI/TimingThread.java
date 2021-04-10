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

package AntGUI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import AntWorld.World;

public final class TimingThread implements Runnable
{
    
    private AntToy controlled;
    private long turns;

    public TimingThread(AntToy controlled)
    {
        this.controlled = controlled;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                long time = controlled.GoMifune.getOpaque();
                if (0 != time)
                {
                    Thread.sleep(time);
                }
            }
            catch (InterruptedException e)
            {
                // I DONT CARE!
            }
            World.RESULT result = controlled.viewer.update();
            if (World.RESULT.WIN == result)
            {
                turns = controlled.viewer.getTurns();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        if (0 == controlled.GoMifune.getOpaque())
                        { // The program becomes unusable to me at fastest speed.
                            controlled.GoMifune.setOpaque(10);
                        }
                        JOptionPane.showMessageDialog(controlled.frame, "You've Won!");
                        // Use logger to synchronize writes to debugConsole.
                        controlled.logger.message("You've Won!\nIt took " + turns + " updates.\n");
                        controlled.frame.setTitle("Ant Toy : You've Won!");
                    }
                });
            }
            else if (World.RESULT.LOSE == result)
            {
                turns = controlled.viewer.getTurns();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        if (0 == controlled.GoMifune.getOpaque())
                        { // The program becomes unusable to me at fastest speed.
                            controlled.GoMifune.setOpaque(10);
                        }
                        JOptionPane.showMessageDialog(controlled.frame, "You've lost.");
                        // Use logger to synchronize writes to debugConsole.
                        controlled.logger.message("You've lost.\nIt took " + turns + " updates.\n");
                        controlled.frame.setTitle("Ant Toy : You've lost.");
                    }
                });
            }
            else if (World.RESULT.BROKEN == result)
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        if (0 == controlled.GoMifune.getOpaque())
                        { // The program becomes unusable to me at fastest speed.
                            controlled.GoMifune.setOpaque(10);
                        }
                        JOptionPane.showMessageDialog(controlled.frame, "You script crashed. See debug screen for details.");
                        controlled.frame.setTitle("Ant Toy : Your script crashed. See debug screen for details.");
                    }
                });
            }
            else
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        controlled.viewer.repaint();
                    }
                });
            }
        }
    }

}
