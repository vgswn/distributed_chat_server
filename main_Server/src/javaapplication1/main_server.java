/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication1;

import com.google.gson.Gson;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Math.random;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import static java.util.Objects.hash;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import sun.misc.BASE64Decoder;

/**
 *
 * @author vips
 */
public class main_server {

    public static volatile HashMap<String, InetAddress> ip = new HashMap<String, InetAddress>();
    public static volatile HashMap<String, InetAddress> tempip = new HashMap<String, InetAddress>();

    Scanner scan = new Scanner(System.in);
    public static String map[] = new String[15];

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static InetAddress getServer(String s) throws UnknownHostException {
        int num = 31;
        int hash = 0;
        for (int j = 0; j < s.length(); j++) {
            hash = hash + num * s.charAt(j);
        }
        hash = hash % 2;
        InetAddress ippp = InetAddress.getByName(map[hash]);
        return ippp;
    }

    main_server() {
        //ob=new message();
        map[0] = "172.26.44.218";
        map[1] = "172.26.45.17";
        map[2] = "172.26.46.244";
        Read t1 = new Read();
        t1.start();
        //System.out.println("ff");

    }

    class message implements Cloneable {

        int from;
        int type;
        String userid1, userid2, groupid, message, pwd;
        String pic;
        int gender;
        boolean ans;
        String group_type;

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        message() throws FileNotFoundException, IOException {
            this.from = 0;
            //this.type=main_server.random.nextInt()%7;
            this.type = 2;
            this.userid1 = "v";
            this.userid2 = "ron";
            this.groupid = "friends";
            this.message = "hi";
            this.gender = 1;
            this.ans = false;
            this.pwd = "new";
            this.group_type = "Public";
            File file = new File("/home/vips/Desktop/zebra.jpg");

            BufferedImage originalImage = ImageIO.read(file);
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            originalImage = main_server.resize(originalImage, 400, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, "jpg", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();

            this.pic = new String(Base64.getEncoder().encode(imageInByte), "UTF-8");
            /*this.piclen=this.pic.length();
            
            BufferedImage image = null;
                               byte[] imageByte;

                               BASE64Decoder decoder = new BASE64Decoder();
                               imageByte = decoder.decodeBuffer(this.pic);
                               ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                               image = ImageIO.read(bis);
                               bis.close();

// write the image to a file
                               File outputfile = new File("image.jpg");
                               ImageIO.write(image, "jpg", outputfile);*/

        }
    }

    public class Read extends Thread {

        public void run() {
            try {
                DatagramSocket ds = new DatagramSocket(5000);
                byte[] buf = new byte[65520];
                DatagramPacket dp = new DatagramPacket(buf, 65520);
                while (true) {
                    ds.receive(dp);
                    InetAddress ipp = dp.getAddress();
                    String str = new String(dp.getData(), 0, dp.getLength());
                    message m = new message();
                    Gson gson = new Gson();
                    m = gson.fromJson(str, message.class);
                    //System.out.println(str);
                    //message mess = new message(m);
                    message mess = (message) m.clone();
                    PreparedStatement stmt, stmt1;
                    ResultSet rs, rs1;

                    if (m.from == 1) {
                        //System.out.println(m.type);
                        switch (m.type) {
                            case 1: {
                                if (m.ans) {
                                    stmt = db_connect.con.prepareStatement("select distinct(userid) from group_data");
                                    rs = stmt.executeQuery();
                                    int i = 1;
                                    mess.from = 2;

                                    while (rs.next()) {
                                        rs.absolute(i++);
                                        String newgroup;
                                        String user = rs.getString(1);
                                        System.out.println(user + " " + m.userid1);
                                        if (user.compareTo(m.userid1) < 0) {
                                            newgroup = m.userid1 + "<->" + user;

                                        } else {
                                            newgroup = user + "<->" + m.userid1;

                                        }
                                        mess.type = 5;
                                        mess.groupid = newgroup;

                                        mess.group_type = "Private";
                                        mess.userid2 = user;
                                        InetAddress ipAdd = getServer(mess.groupid);
                                        

                                            String stri = gson.toJson(mess);
                                        Send t = new Send(ipAdd,stri);
                                        t.start();

                                    }
                                    System.out.println(i);
                                } else {
                                    String stri = gson.toJson(mess);
                                    Send t = new Send(main_server.tempip.get(mess.userid1),stri);
                                    t.start();
                                }
                                main_server.tempip.remove(mess.userid1);

                                break;
                            }
                            case 2: {
                                if (m.ans) {
                                    main_server.ip.put(m.userid1, main_server.tempip.get(m.userid1));
                                    main_server.tempip.remove(mess.userid1);
                                    stmt = db_connect.con.prepareStatement("select groupid from group_data where type=?");
                                    stmt.setString(1, "Public");
                                    rs = stmt.executeQuery();
                                    int i = 1;
                                    String s = "";
                                    while (rs.next()) {
                                        rs.absolute(i++);
                                        s = s + "," + rs.getString(1);

                                    }
                                    stmt = db_connect.con.prepareStatement("select groupid from group_data where userid=? and type=?");
                                    stmt.setString(1, m.userid1);
                                    stmt.setString(2, "Private");
                                    rs = stmt.executeQuery();
                                    i = 1;
                                    while (rs.next()) {
                                        rs.absolute(i++);
                                        s = s + "," + rs.getString(1);

                                    }
                                    mess.message = s;
                                    String stri = gson.toJson(mess);
                                    Send t = new Send(main_server.ip.get(mess.userid1),stri);
                                    t.start();

                                }

                                break;
                            }
                            case 3: {
                                stmt = db_connect.con.prepareStatement("select  userid from group_data where groupid=?");
                                stmt.setString(1, m.groupid);
                                rs = stmt.executeQuery();
                                int i = 1;
                                while (rs.next()) {
                                    rs.absolute(i++);
                                    String user = rs.getString(1);
                                    String stri = gson.toJson(mess);
                                    Send t = new Send(main_server.ip.get(user),stri);
                                    t.start();
                                }

                                break;
                            }
                            case 4: {
                                String stri = gson.toJson(mess);
                                
                                Send t = new Send(main_server.ip.get(mess.userid1),stri);
                               // System.out.println("gfchvjbknlkjgchgjk"+main_server.ip.get(mess.userid1));
                                t.start();
                                break;
                            }
                            case 5: {

                                if (m.ans) {
                                    stmt1 = db_connect.con.prepareStatement("insert into group_data values (?,?,?)");
                                    stmt1.setString(1, m.userid1);
                                    stmt1.setString(2, m.groupid);
                                    stmt1.setString(3, m.group_type);
                                    stmt1.executeUpdate();
                                    if (m.group_type.equals("Private")) {
                                        stmt1 = db_connect.con.prepareStatement("insert into group_data values (?,?,?)");
                                        stmt1.setString(1, m.userid2);
                                        stmt1.setString(2, m.groupid);
                                        stmt1.setString(3, m.group_type);
                                        stmt1.executeUpdate();
                                    }
                                } else {
                                    if (m.group_type.equals("Public")) {
                                        String stri = gson.toJson(mess);
                                        Send t = new Send(main_server.ip.get(mess.userid1),stri);
                                        t.start();
                                    }
                                }
                                break;
                            }
                            case 6: {
                                if (m.ans) {

                                    stmt1 = db_connect.con.prepareStatement("insert into group_data values (?,?,?)");
                                    stmt1.setString(1, m.userid1);
                                    stmt1.setString(2, m.groupid);
                                    stmt1.setString(3, m.group_type);
                                    stmt1.executeUpdate();

                                    stmt = db_connect.con.prepareStatement("select  userid from group_data where groupid=?");
                                    stmt.setString(1, m.groupid);
                                    rs = stmt.executeQuery();
                                    int i = 1;
                                    while (rs.next()) {
                                        rs.absolute(i++);
                                        String user = rs.getString(1);
                                        String stri = gson.toJson(mess);
                                        Send t = new Send(main_server.ip.get(user),stri);
                                        t.start();
                                    }
                                }
                                break;
                            }

                            case 8: {
                                if (m.ans) {
                                    String stri = gson.toJson(mess);
                                    Send t = new Send(main_server.ip.get(mess.userid1),stri);
                                    t.start();
                                }
                                break;
                            }

                        }
                    } else if (m.from == 0) {
                        switch (m.type) {
                            case 1: {
                                if (main_server.tempip.containsKey(m.userid1)) {
                                    
                                    mess.ans = false;
                                    String stri = gson.toJson(mess);
                                    Send t = new Send(ipp,stri);
                                    t.start();
                                } else {
                                    main_server.tempip.put(m.userid1, ipp);
                                    InetAddress x = getServer(m.userid1);
                                    String stri = gson.toJson(mess);
                                    Send t = new Send(x,stri);
                                    t.start();

                                }

                                break;
                            }
                            case 2: {
                                main_server.tempip.put(m.userid1, ipp);
                                InetAddress x = getServer(m.userid1);
                                String stri = gson.toJson(mess);
                                Send t = new Send(x,stri);
                                t.start();

                                break;
                            }
                            case 3: {
                                InetAddress x = getServer(m.groupid);
                                String stri = gson.toJson(mess);
                                Send t = new Send(x, stri);
                                t.start();
                                break;
                            }
                            case 4: {
                                String stri = gson.toJson(mess);
                                
                                InetAddress x = getServer(m.userid2);
                                Send t = new Send(x,stri);
                                t.start();
                                break;
                            }
                            case 5: {
                                mess.group_type = "Public";
                                InetAddress x = getServer(m.groupid);
                                String stri = gson.toJson(mess);
                                Send t = new Send(x,stri);
                                t.start();

                                break;
                            }
                            case 6: {
                                InetAddress x = getServer(m.groupid);
                                String stri = gson.toJson(mess);
                                Send t = new Send(x,stri);
                                t.start();

                                break;
                            }
                            case 7: {
                                stmt = db_connect.con.prepareStatement("select userid from group_data where groupid = ?");
                                stmt.setString(1, m.groupid);
                                rs = stmt.executeQuery();
                                int i = 1;
                                String s = "";
                                while (rs.next()) {
                                    rs.absolute(i++);
                                    s = s + "," + rs.getString(1);
                                }
                                mess.message = s;
                                String stri = gson.toJson(mess);
                                Send t = new Send(ipp,stri);
                                t.start();
                                break;
                            }
                            case 8: {
                                InetAddress x = getServer(m.groupid);
                                String stri = gson.toJson(mess);
                                Send t = new Send(x,stri);
                                t.start();

                                break;
                            }
                        }
                    }

                }
            } catch (SocketException ex) {
                Logger.getLogger(main_server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(main_server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(main_server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(main_server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public class Send extends Thread {

        
        InetAddress ipaddr;

        Send(InetAddress ipaddr, String name) {
            super(name);
            this.ipaddr = ipaddr;
            

        }

        public void run() {
                try {

                    DatagramSocket ds = new DatagramSocket();
                    String str=Thread.currentThread().getName();
                    
                   //System.out.println(ipaddr);

                    DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ipaddr, 3000);
                    ds.send(dp);
                    ds.close();

                } catch (SocketException ex) {
                    Logger.getLogger(main_server.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(main_server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

