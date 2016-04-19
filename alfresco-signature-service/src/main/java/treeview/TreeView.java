package org.tenthline.treeview;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.apache.http.ParseException;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.*;

import java.io.IOException;

public class TreeView extends AbstractWebScript
{

    ServiceRegistry service;

    public TreeView()
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
            NodeRef nd = null;
            if(req.getParameterValues("key") == null)
            {
                StoreRef storeRef = new StoreRef("workspace", "SpacesStore");
                ResultSet resultSet = service.getSearchService().query(storeRef, "lucene", "PATH:\"/app:company_home\"");
                List list = resultSet.getNodeRefs();
                for(Iterator iterator = list.iterator(); iterator.hasNext();)
                {
                    NodeRef nodeRef = (NodeRef)iterator.next();
                    nd = nodeRef;
                }

            } else
            {
                nd = new NodeRef(req.getParameter("key"));
            }
            List list = service.getFileFolderService().list(nd);
            List arjs = new ArrayList();
            JSONObject one = null;
            int count = 1;
            res.getWriter().write("[");
            for(Iterator iterator1 = list.iterator(); iterator1.hasNext();)
            {
                FileInfo fileInfo = (FileInfo)iterator1.next();
                one = new JSONObject();
                arjs.add(one);
                one.put("title", fileInfo.getName());
                one.put("key", fileInfo.getNodeRef().toString());
                if(fileInfo.getType().getLocalName().equals("folder") || fileInfo.getType().getLocalName().equals("sites") || fileInfo.getType().getLocalName().equals("site"))
                {
                    one.put("isFolder", Boolean.TRUE);
                    one.put("isLazy", Boolean.TRUE);
                }
                res.getWriter().write(one.toJSONString());
                if(list.size() != count)
                {
                    res.getWriter().write(",");
                }
                count++;
            }

            res.getWriter().write("]");
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }
    }
}
