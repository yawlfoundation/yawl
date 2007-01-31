package com.nexusbpm.editor.icon;

import java.awt.RenderingHints.Key;
import java.awt.color.ColorSpace;
import java.util.HashMap;
import java.util.Map;

public class IconSpecification{

	private String sourceImageName;
	private Size imageSize;
	private double rotateRadians;
	private String overlayImageName;
	private Size overlayImageSize;
	private int overlayXOffset;
	private int overlayYOffset;
	private float brightness;
	private boolean greyscale;
	
	public enum Size {TINY, SMALL, MEDIUM, LARGE, HUGE, SPLASH};
	
	public IconSpecification() {
		this.setImageSize(Size.MEDIUM);
		this.setBrightness(1.0f);
	}

	public IconSpecification(String sourceImageName, Size imageSize, double rotateRadians, String overlayImageName, Size overlayImageSize, int overlayXOffset, int overlayYOffset, float brightness, boolean greyscale) {
		super();
		this.sourceImageName = sourceImageName;
		this.imageSize = imageSize;
		this.rotateRadians = rotateRadians;
		this.overlayImageName = overlayImageName;
		this.overlayImageSize = overlayImageSize;
		this.overlayXOffset = overlayXOffset;
		this.overlayYOffset = overlayYOffset;
		this.brightness = brightness;
		this.greyscale = greyscale;
	}

	public IconSpecification(String sourceImageName, Size imageSize) {
		this(sourceImageName, imageSize, 0, null, null, 0, 0, 1.0f, false);
	}

	public IconSpecification(String sourceImageName, Size imageSize, float rotateRadians) {
		this(sourceImageName, imageSize, rotateRadians, null, null, 0, 0, 1.0f, false);
	}

	public IconSpecification(String sourceImageName, Size imageSize, String overlayImageName, Size overlayImageSize, int overlayXOffset, int overlayYOffset) {
		this(sourceImageName, imageSize, 0, overlayImageName, overlayImageSize, overlayXOffset, overlayYOffset, 1.0f, false);
	}

	public Size getImageSize() {
		return imageSize;
	}

	public void setImageSize(Size imageSize) {
		this.imageSize = imageSize;
	}

	public String getOverlayImageName() {
		return overlayImageName;
	}

	public void setOverlayImageName(String overlayImageName) {
		this.overlayImageName = overlayImageName;
	}

	public int getOverlayXOffset() {
		return overlayXOffset;
	}

	public void setOverlayXOffset(int overlayXOffset) {
		this.overlayXOffset = overlayXOffset;
	}

	public int getOverlayYOffset() {
		return overlayYOffset;
	}

	public void setOverlayYOffset(int overlayYOffset) {
		this.overlayYOffset = overlayYOffset;
	}

	public double getRotateRadians() {
		return rotateRadians;
	}

	public void setRotateRadians(double rotateRadians) {
		this.rotateRadians = rotateRadians;
	}

	public String getSourceImageName() {
		return sourceImageName;
	}

	public void setSourceImageName(String sourceImageName) {
		this.sourceImageName = sourceImageName;
	}

	public boolean isRotated() {
		return getRotateRadians() != 0.0;
	}

	public boolean isOverlayed() {
		return getOverlayImageName() != null;
	}

	
	public IconSpecification clone() {
		IconSpecification spec = new IconSpecification();
		spec.setImageSize(this.getImageSize());
		spec.setOverlayImageName(this.getOverlayImageName());
		spec.setOverlayImageSize(this.getOverlayImageSize());
		spec.setOverlayXOffset(this.getOverlayXOffset());
		spec.setOverlayYOffset(this.getOverlayYOffset());
		spec.setRotateRadians(this.getRotateRadians());
		spec.setSourceImageName(this.getSourceImageName());
		spec.setGreyscale(this.isGreyscale());
		spec.setBrightness(this.getBrightness());
		return spec;
	}




	public Size getOverlayImageSize() {
		return overlayImageSize;
	}

	public void setOverlayImageSize(Size overlayImageSize) {
		this.overlayImageSize = overlayImageSize;
	}

	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}

	public boolean isGreyscale() {
		return greyscale;
	}

	public void setGreyscale(boolean greyscale) {
		this.greyscale = greyscale;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + Float.floatToIntBits(brightness);
		result = PRIME * result + (greyscale ? 1231 : 1237);
		result = PRIME * result + ((imageSize == null) ? 0 : imageSize.hashCode());
		result = PRIME * result + ((overlayImageName == null) ? 0 : overlayImageName.hashCode());
		result = PRIME * result + ((overlayImageSize == null) ? 0 : overlayImageSize.hashCode());
		result = PRIME * result + overlayXOffset;
		result = PRIME * result + overlayYOffset;
		long temp;
		temp = Double.doubleToLongBits(rotateRadians);
		result = PRIME * result + (int) (temp ^ (temp >>> 32));
		result = PRIME * result + ((sourceImageName == null) ? 0 : sourceImageName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final IconSpecification other = (IconSpecification) obj;
		if (Float.floatToIntBits(brightness) != Float.floatToIntBits(other.brightness))
			return false;
		if (greyscale != other.greyscale)
			return false;
		if (imageSize == null) {
			if (other.imageSize != null)
				return false;
		} else if (!imageSize.equals(other.imageSize))
			return false;
		if (overlayImageName == null) {
			if (other.overlayImageName != null)
				return false;
		} else if (!overlayImageName.equals(other.overlayImageName))
			return false;
		if (overlayImageSize == null) {
			if (other.overlayImageSize != null)
				return false;
		} else if (!overlayImageSize.equals(other.overlayImageSize))
			return false;
		if (overlayXOffset != other.overlayXOffset)
			return false;
		if (overlayYOffset != other.overlayYOffset)
			return false;
		if (Double.doubleToLongBits(rotateRadians) != Double.doubleToLongBits(other.rotateRadians))
			return false;
		if (sourceImageName == null) {
			if (other.sourceImageName != null)
				return false;
		} else if (!sourceImageName.equals(other.sourceImageName))
			return false;
		return true;
	}

	
	
}
