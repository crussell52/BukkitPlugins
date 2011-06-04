package crussell52.poi;

import java.util.ArrayList;

import org.bukkit.Location;

public class PagedPoiList {
	
	public static final int TYPE_AREA_SEARCH = 0;
	public static final int TYPE_OWNER_LIST = 1;
	
	private int _maxPerPage;
	private ArrayList<ArrayList<Poi>> _pages;
	private int _currentPage = 1;
	private int _listType = TYPE_AREA_SEARCH;
	
	@SuppressWarnings("unused")
	private PagedPoiList() {
		// hide the default constructor
	}
	
	public PagedPoiList(int maxPerPage, ArrayList<Poi> results, int listType) {
		
		if (maxPerPage < 1) {
			throw new IndexOutOfBoundsException("Can not have less than 1 POI per page.");
		}
		
		this._maxPerPage = maxPerPage;
		this._pages = _pageResults(results);
		this._listType = listType;
	}
	
	public int getListType()
	{
		return this._listType;
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
	
	public ArrayList<String> getPageReport(Location location, int distanceThreshold) 
	{
		// get page report for the current page, using long summary
		return this._getPageReport(false, location, distanceThreshold);
	}
	
	public ArrayList<String> getPageReport() 
	{
		// get page report for the current page, using short summary
		return this._getPageReport(true, null, null);
	}

	private ArrayList<String> _getPageReport(boolean useShortSummary, Location location, Integer distanceThreshold) 
	{
		// pull out the relevant page
		ArrayList<Poi> poiList = this.getPage(this._currentPage - 1);
		
		// create a list to hold the message lines
		ArrayList<String> report = new ArrayList<String>();
	
		// we have results, get the number of pages and total result count
		int numPages = this.getNumPages();
		int numResults = this.getTotalCount();
		
		// start by producing the header.
		report.add("\u00a72" + numResults + " POIs found. \u00a7e(Page " + this._currentPage + " of " + numPages + ")");

		int summaryIndex = 0;
		String colorCode;
		for (Poi poi : poiList) {
			colorCode = (++summaryIndex % 2) == 0 ? "\u00a77" : "";
			if (useShortSummary) {
				report.add(poi.getShortSummary(colorCode));
			}
			else {
				report.addAll(poi.getSummary(location, distanceThreshold, colorCode));
			}
		}
		
		return report;
	}
	
	public boolean setPage(int pageNum) {
		if (pageNum < 1 || pageNum > this.getNumPages()) {
			return false;
		}
		
		this._currentPage = pageNum;
		return true;
	}
	
	public void firstPage() {
		this._currentPage = 1;
	}
	
	public void previousPage() {
		if (this._currentPage > 1) {
			this._currentPage--;
		}
	}
	
	public void lastPage()
	{
		this._currentPage = this.getNumPages();
	}
	
	public void nextPage()
	{
		if (this._currentPage < this.getNumPages()) {
			this._currentPage++;
		}
	}
}
