package org.twinone.locker.appselect;

import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

/**
 * Represents a Application or a Separator
 * 
 * @author twinone
 * 
 */
public class AppListElement implements Comparable<AppListElement> {

	public String title;
	// null if not an activity
	private final PackageItemInfo pii;
	// null if not an activity
	public final String packageName;
	private Drawable mIcon;

	/**
	 * Indicates the priority of this item. The higher the priority, the higher
	 * it will appear in the list
	 */
	public final int priority;
	public static final int PRIORITY_IMPORTANT_CATEGORY = 7;
	public static final int PRIORITY_IMPORTANT_APPS = 6;
	public static final int PRIORITY_SYSTEM_CATEGORY = 5;
	public static final int PRIORITY_SYSTEM_APPS = 4;
	public static final int PRIORITY_NORMAL_CATEGORY = 3;
	public static final int PRIORITY_NORMAL_APPS = 1;

	public boolean locked = true;

	public Drawable getIcon(PackageManager pm) {
		if (mIcon == null) {
			if (pii == null)
				return null;
			mIcon = pii.loadIcon(pm);
		}
		return mIcon;
	}

	public String getLabel(PackageManager pm) {
		if (title == null) {
			title = (String) pii.loadLabel(pm);
		}
		return title;
	}

	public AppListElement(String label, PackageItemInfo pii, int priority) {
		this.title = label;
		this.pii = pii;
		this.packageName = pii.packageName;
		this.priority = priority;
	}

	/** For separators */
	public AppListElement(String label, int priority) {
		this.title = label;
		this.pii = null;
		this.packageName = "";
		this.priority = priority;

	}

	/** For non activity apps */
	public AppListElement(String label, String packageName, int priority) {
		this.title = label;
		this.pii = null;
		this.packageName = packageName;
		this.priority = priority;

	}

	public boolean isApp() {
		return packageName != null && packageName.length() > 0;
	}

	@Override
	public final boolean equals(Object object) {
		if (object == null)
			return false;
		if (!(object instanceof AppListElement))
			return false;
		AppListElement sh = (AppListElement) object;
		if (isApp() != sh.isApp())
			return false;
		if (!isApp()) {
			return title != null && title.equals(sh.title);
		}
		return packageName != null && packageName.equals(sh.packageName);
	}

	@Override
	public int hashCode() {
		if (isApp()) {
			return new StringBuilder("bypkgname").append(packageName)
					.toString().hashCode();
		}
		return new StringBuilder("bytitle").append(title).toString().hashCode();
	}

	@Override
	public int compareTo(AppListElement o) {
		if (priority != o.priority)
			return o.priority - priority;

		if (this.locked != o.locked)
			return this.locked ? -1 : 1;
		if (this.title == null || o.title == null) {
			return 0;
		}
		return this.title.compareTo(o.title);
	}
}
