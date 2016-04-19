package org.tenthline.treeview;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.http.ParseException;
import org.springframework.extensions.webscripts.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Picture extends AbstractWebScript
{

    ServiceRegistry service;

    public Picture()
    {
    }

    public void setServiceRegistry(ServiceRegistry service)
    {
        this.service = service;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
        throws IOException
    {
        try
        {
            try
            {
                String pages[] = req.getParameter("pagenumber").split(",");
                int insertAt = Integer.valueOf(pages[0]).intValue();
                NodeRef noderefSource = new NodeRef(req.getParameter("sourcenodeRef"));
                ContentReader ctnodeRefSource = service.getFileFolderService().getReader(noderefSource);
                FileChannel channel = ctnodeRefSource.getFileChannel();
                java.nio.ByteBuffer buf = channel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0L, channel.size());
                PDFFile pdffile = new PDFFile(buf);
                PDFPage page = pdffile.getPage(insertAt);
                Rectangle rect = new Rectangle(0, 0, (int)page.getBBox().getWidth(), (int)page.getBBox().getHeight());
                java.awt.Image img = page.getImage(220, 250, rect, null, true, true);
                BufferedImage bufferedImage = new BufferedImage(rect.width, rect.height, 1);
                Graphics g = bufferedImage.createGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();
                ImageIO.write(bufferedImage, "jpg", res.getOutputStream());
                bufferedImage.flush();
                channel.close();
            }
            catch(IOException e)
            {
                throw new AlfrescoRuntimeException(e.getMessage(), e);
            }
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }
    }
}
