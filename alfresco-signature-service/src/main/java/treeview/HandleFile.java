package org.tenthline.treeview;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.*;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.util.TempFileProvider;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.extensions.webscripts.*;

import java.util.Iterator;
import java.util.List;

public class HandleFile extends AbstractWebScript
{

    ServiceRegistry service;

    public HandleFile()
    {
    }

    public void setServiceRegistry(ServiceRegistry service)
    {
        this.service = service;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res)
        throws IOException
    {
        PDDocument pdf;
        InputStream is;
        File tempDir;
        File tempFile;
        NodeRef savefolder;
        String namefilefinal;
        pdf = null;
        is = null;
        InputStream cis = null;
        tempDir = null;
        tempFile = null;
        ContentWriter writer = null;
        savefolder = null;
        String namefile = null;
        namefilefinal = null;
        try
        {
            String pages[] = req.getParameter("pagenumber").split(",");
            int insertAt = Integer.valueOf(pages[0]).intValue();
            int x = Integer.valueOf(req.getParameter("cordinate_x")).intValue();
            int y = Integer.valueOf(req.getParameter("cordinate_y")).intValue();
            NodeRef noderefSource = new NodeRef(req.getParameter("sourcenodeRef"));
            NodeRef noderef = new NodeRef(req.getParameter("nodeRef"));
            ContentReader ctnodeRef = service.getFileFolderService().getReader(noderef);
            ContentReader ctnodeRefSource = service.getFileFolderService().getReader(noderefSource);
            is = ctnodeRefSource.getContentInputStream();
            cis = ctnodeRef.getContentInputStream();
            byte imagebytes[] = new byte[50000];
            cis.read(imagebytes);
            File alfTempDir = TempFileProvider.getTempDir();
            tempDir = new File((new StringBuilder(String.valueOf(alfTempDir.getPath()))).append(File.separatorChar).append(noderefSource.getId()).toString());
            tempDir.mkdir();
            String fileName = "";
            if(!service.getNodeService().getParentAssocs(noderefSource).isEmpty())
            {
                ChildAssociationRef ref = (ChildAssociationRef)service.getNodeService().getParentAssocs(noderefSource).get(0);
                savefolder = ref.getParentRef();
            }
            if(!fileName.equals(""))
            {
                tempFile = new File((new StringBuilder(String.valueOf(alfTempDir.getPath()))).append(File.separatorChar).append(noderefSource.getId()).append(File.separatorChar).append(fileName).append(".pdf").toString());
            } else
            {
                namefile = service.getFileFolderService().getFileInfo(noderefSource).getName();
                List files = service.getFileFolderService().listFiles(savefolder);
label0:
                for(int i = 1; i <= 10000; i++)
                {
                    String splittedname[] = namefile.split("[.]");
                    namefilefinal = (new StringBuilder(String.valueOf(splittedname[0]))).append("-").append(i).append(".").append(splittedname[1]).toString();
                    for(Iterator iterator = files.iterator(); iterator.hasNext();)
                    {
                        FileInfo file = (FileInfo)iterator.next();
                        if(file.getName().equals(namefilefinal))
                        {
                            continue label0;
                        }
                    }

                    break;
                }

                tempFile = new File((new StringBuilder(String.valueOf(alfTempDir.getPath()))).append(File.separatorChar).append(noderefSource.getId()).append(File.separatorChar).append(namefilefinal).toString());
            }
            tempFile.createNewFile();
            Image image = Image.getInstance(imagebytes);
            PdfReader pdfReader = new PdfReader(is);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(tempFile));
            for(int i = 0; i < pages.length; i++)
            {
                insertAt = Integer.valueOf(pages[i]).intValue();
                PdfContentByte content = pdfStamper.getUnderContent(insertAt);
                content = pdfStamper.getOverContent(insertAt);
                image.scalePercent(50F);
                image.setAbsolutePosition(x, y);
                content.addImage(image);
            }

            pdfStamper.close();
            pdfReader.close();
            File afile[];
            int k = (afile = tempDir.listFiles()).length;
            for(int j = 0; j < k; j++)
            {
                File file = afile[j];
                try
                {
                    if(file.isFile())
                    {
                        NodeRef destinationNode = createDestinationNode(file.getName(), savefolder, noderefSource);
                        writer = service.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
                        writer.setEncoding(ctnodeRef.getEncoding());
                        writer.setMimetype("application/pdf");
                        writer.putContent(file);
                        file.delete();
                    }
                }
                catch(FileExistsException e)
                {
                    throw new AlfrescoRuntimeException("Failed to process file.", e);
                }
            }
        }
        catch(IOException e)
        {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }
        catch(DocumentException e1)
        {
            e1.printStackTrace();
        }
        if(pdf != null)
        {
            try
            {
                pdf.close();
            }
            catch(IOException e)
            {
                throw new AlfrescoRuntimeException(e.getMessage(), e);
            }
        }
        if(is != null)
        {
            try
            {
                is.close();
            }
            catch(IOException e)
            {
                throw new AlfrescoRuntimeException(e.getMessage(), e);
            }
        }
        if(tempDir != null)
        {
            tempFile.delete();
            tempDir.delete();
        }
        res.getWriter().write("File has been generated sucecessfully.");

    }

    private NodeRef createDestinationNode(String filename, NodeRef destinationParent, NodeRef target)
    {
        NodeService nodeService = service.getNodeService();
        FileInfo fileInfo = null;
        fileInfo = service.getFileFolderService().create(destinationParent, filename, nodeService.getType(target));
        NodeRef destinationNode = fileInfo.getNodeRef();
        return destinationNode;
    }
}
