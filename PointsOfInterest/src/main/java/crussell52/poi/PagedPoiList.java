package crussell52.poi;

import java.util.ArrayList;

import org.bukkit.Location;

public class PagedPoiList {
	
	private int _maxPerPage;
	private ArrayList<ArrayList<Poi>> _pages;
	
	@SuppressWarnings("unused")
	private PagedPoiList() {
		// hide the default constructor
	}
	
	public PagedPoiList(int maxPerPage, ArrayList<Poi> results) {
		
		if (maxPerPage < 1) {
			throw new IndexOutOfBoundsException("Can not have less than 1 POI per page.");
		}
		
		this._maxPerPage = maxPerPage;
		this._pages = _pageResults(results);
	}
	
	public int getTotalCount() {
		
		int total = 0;
		for (ArrayList<Poi> pageContents : this._pages) {
			total += pageContents.size();
		}

		return total;
	}
	
	public int getNumPages() {
		return this._pages.size();
	}
	
	public ArrayList<Poi> getPage(int pageNum) {
		return this._pages.get(pageNum);
	}
	
	private ArrayList<ArrayList<Poi>> _pageResults(ArrayList<Poi> poiList) {
		
		ArrayList<ArrayList<Poi>> pagedList = new ArrayList<ArrayList<Poi>>();
		
		// handle the easy case first... everything fits on the first page.
		if (poiList.size() < this._maxPerPage) {
			pagedList.add(poiList);
			return pagedList;
		}

		// we have more than 1 page worth of POIs... so we need to go through the paging process
		ArrayList<Poi> page = new ArrayList<Poi>();
		
		// loop over each POI in the list
		for (Poi poi : poiList) {
			// if our page is already at the max, then add it to the paged list
			// and start a new page.
			if (page.size() == this._maxPerPage) {
				pagedList.add(page);
				page = new ArrayList<Poi>();
			}
			
			// add the POI to the page.
			page.add(poi);
		}
		
		// we'll always have one page that wasn't added...
		// add it then return the paged list.
		pagedList.add(page);
		return pagedList;
	}
	
	public ArrayList<String> getPageReport(int pageNum, Location location, int distanceThreshold) 
	{
		// create a list to hold the message lines
		ArrayList<String> report = new ArrayList<String>();
	
		// we have results, get the number of pages and total result count
		int numPages = this.getNumPages();
		int numResults = this.getTotalCount();
		
		// make sure the page number does not exceed the number of pages.
		if (pageNum > numPages) {
			report.add("Can't display page " + pageNum + "...");
			report.add("Only " + numPages + " page(s) in result list.");
			return report;
		}
		
		
		// start by producing the header.
		report.add("\u00a72" + numResults + " POIs found. \u00a7e(Page " + pageNum + " of " + numPages + ")");

		ArrayList<Poi> poiList = this.getPage(pageNum - 1);
		
		int summaryIndex = 0;
		String colorCode;
		for (Poi poi : poiList) {
			colorCode = (++summaryIndex % 2) == 0 ? "\u00a77" : "";
			report.addAll(poi.getSummary(location, distanceThreshold, colorCode));
		}
		
		return report;
	}
	
}
