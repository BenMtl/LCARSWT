package de.tucottbus.kt.lcars.net;

import java.awt.geom.Area;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Vector;

import de.tucottbus.kt.lcars.IPanel;
import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.PanelData;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.feedback.UserFeedback;
import de.tucottbus.kt.lcars.util.LoadStatistics;
import de.tucottbus.kt.lcars.util.ObjectSize;

/**
 * RMI network adapter for an {@linkplain Screen LCARS screen}.
 *  
 * @author Matthias Wolff
 */
public class RmiScreenAdapter extends RmiAdapter implements IScreen, IRmiScreenAdapterRemote
{
  // -- Fields --

  /**
   * The memory statistics of the update data transferred by {@link #update(PanelData, boolean)}.
   */
  private Vector<Integer> memStat;
  
  /**
   * The {@linkplain Screen LCARS screen} wrapped by this network adapter.
   */
  private Screen screen;
  
  // -- Constructors --

  /**
   * Creates a new RMI network adapter for a remote {@linkplain Screen LCARS screen}.
   * 
   * @param screen
   *          The screen to wrap by the adapter.
   * @param panelHostName
   *          The name of the host serving the peer {@linkplain IPanel LCARS panel} to connect this
   *          adapter to.
   * @throws RemoteException
   *           If the RMI registry could not be contacted.
   * @throws MalformedURLException
   *           If <code>rmiSelfName</code> is not an appropriately formatted URL.
   */
  public RmiScreenAdapter(Screen screen, String panelHostName)
  throws RemoteException, MalformedURLException
  {
    super(panelHostName);
    this.screen = screen;
  }
  
  // -- Implementation of abstract methods --
  
  @Override
  protected void updatePeer()
  {
    screen.setPanel(getPanel());
  }
  
  @Override
  public String getRmiName()
  {
    return makeScreenAdapterRmiName(getPeerHostName(),0);
  }
  
  @Override
  public String getRmiUrl()
  {
    return makeScreenAdapterUrl(getPeerHostName(),LCARS.getHostName(),0);
  }
  
  @Override
  public String getRmiPeerUrl()
  {
    return makePanelAdapterUrl(getPeerHostName(),LCARS.getHostName(),0);
  }
  
  // -- Implementation of the IRmiScreenAdapter interface --
  
  @Override
  public void ping()
  {
    // Called just to see if screen adapter is still connected. RMI will throw
    // a RemoteException if not.
  }
  
  @Override
  public int getMemStat()
  {
    if (memStat==null) return 0;
    if (memStat.size()==0) return 0;
    int sum = 0;
    for (Integer mem : memStat) sum += mem;
    return sum/memStat.size();
  }
  
  // -- Screen wrapper methods / Implementation of the IScreen interface --

  @Override
  public Area getArea()
  {
    return screen.getArea();
  }
   
  @Override
  public String getHostName()
  {
    return LCARS.getHostName();
  }
  
  @Override
  public void setPanel(String className) throws ClassNotFoundException
  {
    IRmiPanelAdapterRemote rpanel = (IRmiPanelAdapterRemote)getPeer();
    if (rpanel==null)
      // TODO: Better throw an exception?
      return;
   
    try
    {
      rpanel.setPanel(className);
    }
    catch (RemoteException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public IPanel getPanel()
  {
    return (IPanel)getPeer();
  }

  @Override
  public void update(PanelData data, boolean incremental)
  {
    // Do network load statistics 
    if (memStat==null) memStat = new Vector<Integer>(11);
    memStat.add(new Integer(ObjectSize.getSerializedSize(data)));
    if (memStat.size()>10) memStat.remove(0);

    // Do screen update
    screen.update(data,incremental);
  }

  @Override
  public void userFeedback(UserFeedback.Type type)
  {
    screen.userFeedback(type);
  }

  @Override
  public LoadStatistics getLoadStatistics()
  {
    return screen.getLoadStatistics();
  }

  @Override
  public void exit() throws RemoteException
  {
    screen.exit();
  }

}

// EOF
