// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.webtest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Service;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Service
@Path("hello")
@Api(value = "hello", basePath = "/services", description = "hello services")
public class SayHelloService {

    @GET
    @Path("/{who}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "sayHello", notes = "return a plain text with hello + the name you set as a param")
    public String sayHello(@PathParam("who") @ApiParam(name = "who", value = "your name") String who) {
        return "hello " + who;
    }

}
