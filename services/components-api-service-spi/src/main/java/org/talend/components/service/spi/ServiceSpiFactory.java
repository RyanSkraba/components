// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.service.spi;

import java.util.ServiceLoader;
import org.talend.components.api.ComponentInstaller;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.service.common.DefinitionRegistry;

/**
 * Provide a {@link DefinitionRegistry} and {@link ComponentService} based on the jars currently
 * available in the classpath.
 */
public class ServiceSpiFactory {

  /** Singleton for definition registry.  This will be reloaded if set to null. */
  private static DefinitionRegistry defReg;

  /** Singleton for the component service.  This will be reloaded if set to null. */
  private static ComponentService componentService;

  static {
    // Ensure that the definition registry is created when the class is loaded.
    getDefinitionRegistry();
  }

  public static DefinitionRegistry getDefinitionRegistry() {
    if (defReg == null) {
      // Create a new instance of the definition registry.
      DefinitionRegistry reg = new DefinitionRegistry();
      for (ComponentInstaller installer : ServiceLoader.load(ComponentInstaller.class)) {
        installer.install(reg);
      }
      reg.lock();
      // Only assign it to the singleton after being locked.
      defReg = reg;
    }
    return defReg;
  }

  public static ComponentService getComponentService() {
    if (componentService == null) {
      componentService = new ComponentServiceImpl(getDefinitionRegistry());
    }
    return componentService;
  }
}
