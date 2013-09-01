package crussell52.poi;

import java.util.ArrayList;

import crussell52.poi.api.IPoi;
import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 * A paged list of Poi instances.
 *
 * Used to display area search results and owner lists.
 */
public class PagedPoiList {

	/**
	 * Used to indicate that this instance represents a list
	 * of POIs obtained as part of an area search.
	 */
	public static final int TYPE_AREA_SEARCH = 0;

	/**
	 * Used to indicate that this instance represents a list
	 * of POIs obtained as part of an area search.
	 */
	public static final int TYPE_OWNER_LIST = 1;

	/**
	 * Maximum Poi instances per page.
	 */
	private int _maxPerPage;

	/**
	 * List of pages - each page is a list of <code>Poi</code> instances.
	 *
	 * There will always be at least one page. In the case of an empty list
	 * there will be exactly one page with exactly zero Poi instances in it.
	 */
	private ArrayList<ArrayList<Poi>> _pages;

	/**
	 * The page currently being "viewed"
	 *
	 * @see PagedPoiList#firstPage();
	 * @see PagedPoiList#lastPage();
	 * @see PagedPoiList#nextPage();
	 * @see PagedPoiList#previousPage();
	 * @see PagedPoiList#getPageReport();
	 */
	private int _currentPage = 1;

	/**
	 * Indicates which type of list this is.
	 *
	 * Affects the way page reports are generated.
	 */
	private int _listType = TYPE_AREA_SEARCH;

	@SuppressWarnings("unused")
	private PagedPoiList() {
		// hide the default constructor
	}

	/**
	 * Creates a new instance
	 *
	 * @param maxPerPage maximum POIs to hold in each page
	 * @param results A list of <code>Poi</code> instances to page.
	 * @param listType What type of list this is.
	 *
	 * @see PagedPoiList#TYPE_AREA_SEARCH
	 * @see PagedPoiList#TYPE_OWNER_LIST
	 */
	public PagedPoiList(int maxPerPage, ArrayList<Poi> results, int listType) {

		if (maxPerPage < 1) {
			throw new IndexOutOfBoundsException("Can not have less than 1 POI per page.");
		}

		this._maxPerPage = maxPerPage;
		this._pages = _pageResults(results);
		this._listType = listType;
	}

	/**
	 * Indicates what type of data is contained within this list.
	 *
	 * @see PagedPoiList#TYPE_AREA_SEARCH
	 * @see PagedPoiList#TYPE_OWNER_LIST
	 */
	public int getListType()
	{
		return this._listType;
	}

	/**
	 * Total number of POIs, across all pages.
	 */
	public int getTotalCount() {

		int total = 0;
		for (ArrayList<Poi> pageContents : this._pages) {
			total += pageContents.size();
		}

		return total;
	}

	/**
	 * Total number of pages.
	 *
	 * Even empty results have one page.
	 */
	public int getNumPages() {
		return this._pages.size();
	}

	/**
	 * Takes in a list of <code>Poi</code> instances and breaks them
	 * apart into pages.
	 *
	 * @param poiList POIs to page.
	 */
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

	/**
	 * Get a report on the current page which includes distance and directions.
	 *
	 * @param location Location against which distance is calculated
	 * @param distanceThreshold Maximum distance at which directions are available.
	 */
	public ArrayList<String> getPageReport(Location location, int distanceThreshold)
	{
		// get page report for the current page, using long summary
		return this._getPageReport(false, location, distanceThreshold);
	}

	/**
	 * Get a simple report on the current page which includes only name, id, and owner
	 * of each POI.
	 */
	public ArrayList<String> getPageReport()
	{
		// get page report for the current page, using short summary
		return this._getPageReport(true, null, null);
	}

	/**
	 * Generates a report of the current page in the form of a list of strings.
	 *
	 * @param useShortSummary Determines whether a short summary or standard summary is output
	 * @param location Location against which distance is calculated; only relevant is <code>useShortSummary</code> is <code>false</code>.
	 * @param distanceThreshold distanceThreshold Maximum distance at which directions are available; only relevant is <code>useShortSummary</code> is <code>false</code>.
	 */
	private ArrayList<String> _getPageReport(boolean useShortSummary, Location location, Integer distanceThreshold)
	{
		// pull out the relevant page
		ArrayList<Poi> poiList = this._pages.get(this._currentPage - 1);

		// create a list to hold the message lines
		ArrayList<String> report = new ArrayList<String>();

		// we have results, get the number of pages and total result count
		int numPages = this.getNumPages();
		int numResults = this.getTotalCount();

		// start by producing the header.
		report.add(ChatColor.GREEN + (this._listType == TYPE_AREA_SEARCH ? "Area Search: " : "Owner List: ") +
			ChatColor.DARK_GREEN + numResults + " POIs found. " +
			ChatColor.YELLOW + "(Page " + this._currentPage + " of " + numPages + ")");

		int summaryIndex = 0;
		ChatColor colorCode;
		for (Poi poi : poiList) {
			colorCode = (++summaryIndex % 2) == 0 ? ChatColor.GRAY : ChatColor.WHITE;
			if (useShortSummary) {
				report.add(poi.getShortSummary(colorCode));
			}
			else {
				report.addAll(poi.getSummary(location, distanceThreshold, colorCode));
			}
		}

		return report;
	}

	/**
	 * Used to set the current page to a specific page number.
	 *
	 * @param pageNum The page number to set it to
     *
	 * @return If an invalid page number is provided, <code>false</code> will be returned.
	 */
	public boolean setPage(int pageNum) {
		if (pageNum < 1 || pageNum > this.getNumPages()) {
			return false;
		}

		this._currentPage = pageNum;
		return true;
	}

	/**
	 * Used to set the current page to the first page.
	 */
	public void firstPage() {
		this._currentPage = 1;
	}

	/**
	 * used to set the current page to the previous page.
	 *
	 * If the current page is the first page, it will remain the first page.
	 */
	public void previousPage() {
		if (this._currentPage > 1) {
			this._currentPage--;
		}
	}

	/**
	 * Used to set the current page to the last page.
	 */
	public void lastPage()
	{
		this._currentPage = this.getNumPages();
	}

	/**
	 * Used to set the current page to the next page.
	 *
	 * If the current page is the last page, it will remain the last page.
	 */
	public void nextPage()
	{
		if (this._currentPage < this.getNumPages()) {
			this._currentPage++;
		}
	}
}