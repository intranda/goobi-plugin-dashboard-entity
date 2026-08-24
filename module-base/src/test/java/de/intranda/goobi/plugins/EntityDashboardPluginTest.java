package de.intranda.goobi.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IWorkflowPlugin;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.forms.NavigationForm;

public class EntityDashboardPluginTest {

    private static final String ENTITY_EDITOR = "intranda_workflow_entity_editor";
    private static final String OTHER_WORKFLOW_PLUGIN = "intranda_workflow_charite_importer";

    private EntityDashboardPlugin dashboard;

    /** stands in for the workflow plugins that are installed on the server */
    private Map<String, IWorkflowPlugin> installedPlugins;

    /** stands in for the workflow plugin that the window scoped NavigationForm currently holds */
    private IWorkflowPlugin openedPlugin;

    private NavigationForm form;

    @Before
    public void setUp() {
        dashboard = new EntityDashboardPlugin();

        installedPlugins = new HashMap<>();
        installedPlugins.put(ENTITY_EDITOR, new TestWorkflowPlugin(ENTITY_EDITOR));
        installedPlugins.put(OTHER_WORKFLOW_PLUGIN, new TestWorkflowPlugin(OTHER_WORKFLOW_PLUGIN));

        form = mock(NavigationForm.class);
        when(form.getWorkflowPlugin()).thenAnswer(invocation -> openedPlugin);
        when(form.setPlugin(anyString())).thenAnswer(invocation -> {
            openedPlugin = installedPlugins.get(invocation.getArgument(0, String.class));
            return openedPlugin.getGui();
        });
    }

    @Test
    public void testEntityEditorIsLoadedWhenNoWorkflowPluginIsOpened() {
        openedPlugin = null;

        IWorkflowPlugin plugin = dashboard.getEntityEditorPlugin(form);

        assertEquals(ENTITY_EDITOR, plugin.getTitle());
    }

    @Test
    public void testOpenedEntityEditorIsReused() {
        openedPlugin = installedPlugins.get(ENTITY_EDITOR);

        IWorkflowPlugin plugin = dashboard.getEntityEditorPlugin(form);

        // the same instance, so that the breadcrumbs of the running session survive
        assertSame(installedPlugins.get(ENTITY_EDITOR), plugin);
        verify(form, never()).setPlugin(anyString());
    }

    @Test
    public void testOtherOpenedWorkflowPluginIsReplacedByTheEntityEditor() {
        openedPlugin = installedPlugins.get(OTHER_WORKFLOW_PLUGIN);

        IWorkflowPlugin plugin = dashboard.getEntityEditorPlugin(form);

        assertEquals(ENTITY_EDITOR, plugin.getTitle());
    }

    private static class TestWorkflowPlugin implements IWorkflowPlugin {

        private static final long serialVersionUID = 1L;

        private final String title;

        TestWorkflowPlugin(String title) {
            this.title = title;
        }

        @Override
        public PluginType getType() {
            return PluginType.Workflow;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getGui() {
            return "/uii/" + title + ".xhtml";
        }
    }
}
