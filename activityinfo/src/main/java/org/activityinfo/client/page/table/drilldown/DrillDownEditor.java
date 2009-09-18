package org.activityinfo.client.page.table.drilldown;

import org.activityinfo.client.AppEvents;
import org.activityinfo.client.EventBus;
import org.activityinfo.client.command.CommandService;
import org.activityinfo.client.command.callback.Got;
import org.activityinfo.client.common.grid.AbstractGridPlace;
import org.activityinfo.client.event.PivotCellEvent;
import org.activityinfo.client.page.entry.SiteEditor;
import org.activityinfo.client.util.IStateManager;
import org.activityinfo.shared.command.GetSchema;
import org.activityinfo.shared.command.GetSites;
import org.activityinfo.shared.date.DateUtil;
import org.activityinfo.shared.dto.ActivityModel;
import org.activityinfo.shared.dto.IndicatorModel;
import org.activityinfo.shared.dto.Schema;
import org.activityinfo.shared.dto.SiteModel;
import org.activityinfo.shared.report.content.EntityCategory;
import org.activityinfo.shared.report.content.PivotTableData;
import org.activityinfo.shared.report.model.DimensionType;
import org.activityinfo.shared.report.model.Filter;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.store.ListStore;
/*
 * @author Alex Bertram
 */

public class DrillDownEditor extends SiteEditor {

    public interface View extends SiteEditor.View {

        void show(SiteEditor presenter, ActivityModel activity, IndicatorModel indicator,
                  ListStore<SiteModel> store);

    }

    private final EventBus eventBus;
    private final CommandService service;
    private final DateUtil dateUtil;
    private final View view;
    private Listener<PivotCellEvent> eventListener;

    public DrillDownEditor(EventBus eventBus, CommandService service, IStateManager stateMgr, DateUtil dateUtil,
                   View view) {
        super(eventBus, service, stateMgr, view);

        this.eventBus = eventBus;
        this.dateUtil = dateUtil;
        this.service = service;
        this.view = view;

        eventListener = new Listener<PivotCellEvent>() {
            public void handleEvent(PivotCellEvent be) {
                onDrillDown(be);
            }
        };
        eventBus.addListener(AppEvents.Drilldown, eventListener);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        eventBus.removeListener(AppEvents.Drilldown, eventListener);
    }

    public void onDrillDown(PivotCellEvent event) {

        // construct our filter from the intersection of rows and columns
        Filter filter = new Filter(filterFromAxis(event.getRow()), filterFromAxis(event.getColumn()));

        // apply the effective filter
        final Filter effectiveFilter = new Filter(filter, event.getElement().getContent().getEffectiveFilter());

        // determine the indicator
        final int indicatorId = effectiveFilter.getRestrictions(DimensionType.Indicator).iterator().next();
        effectiveFilter.clearRestrictions(DimensionType.Indicator);

        service.execute(new GetSchema(), null, new Got<Schema>() {

            @Override
            public void got(Schema schema) {

                ActivityModel activity = schema.getActivityByIndicatorId(indicatorId);
                IndicatorModel indicator = activity.getIndicatorById(indicatorId);

                drill(activity, indicator, effectiveFilter);
            }
        });
    }

    public void drill(ActivityModel activity, IndicatorModel indicator, Filter filter) {


        currentActivity = activity;

        GetSites cmd = GetSites.byActivity(currentActivity.getId());
        cmd.setPivotFilter(filter);

        loader.setCommand(cmd);
        loader.setSortField(indicator.getPropertyName());
        loader.setSortDir(Style.SortDir.DESC);

        view.show(this, currentActivity, indicator, store);

        loader.load();
    }


    private Filter filterFromAxis(PivotTableData.Axis axis) {

        Filter filter = new Filter();
        while(axis != null) {
            if(axis.getDimension() != null) {
                if(axis.getDimension().getType() == DimensionType.Date) {
                    filter.setDateRange(dateUtil.rangeFromCategory(axis.getCategory()));
                } else if(axis.getCategory() instanceof EntityCategory) {
                    filter.addRestriction(axis.getDimension().getType(), ((EntityCategory)axis.getCategory()).getId());
                }
            }
            axis = axis.getParent();
        }
        return filter;
    }

    @Override
    protected void firePageEvent(AbstractGridPlace place, LoadEvent le) {
        // no page events, we're embedded in another page
    }
}

