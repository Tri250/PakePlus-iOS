export interface HSLColor {
  hue: number;
  sat: number;
  lum: number;
}

export interface ColorGradingGroup {
  hue: number;
  sat: number;
  lum: number;
  blending: number;
  balance: number;
}

export interface CurvePoints {
  rgb: number[][];
  red: number[][];
  green: number[][];
  blue: number[][];
  luma: number[][];
}

export interface Adjustments {
  exposure: number;
  brightness: number;
  contrast: number;
  highlights: number;
  shadows: number;
  whites: number;
  blacks: number;
  temperature: number;
  tint: number;
  saturation: number;
  vibrance: number;
  toneMapper: 'basic' | 'agx';
  sharpness: number;
  clarity: number;
  dehaze: number;
  structure: number;
  texture: number;
  lumaNoise: number;
  colorNoise: number;
  chromaticAberrationRC: number;
  chromaticAberrationBY: number;
  vignetteAmount: number;
  vignetteFeather: number;
  vignetteMidpoint: number;
  grainAmount: number;
  grainRoughness: number;
  grainSize: number;
  lutIntensity: number;
  cropX: number;
  cropY: number;
  cropW: number;
  cropH: number;
  rotation: number;
  flipH: boolean;
  flipV: boolean;
  lensDistortion: number;
  lensVignette: number;
  lensTCA: number;
  curvePoints: CurvePoints;
  hsl: {
    red: HSLColor;
    orange: HSLColor;
    yellow: HSLColor;
    green: HSLColor;
    aqua: HSLColor;
    blue: HSLColor;
    purple: HSLColor;
    magenta: HSLColor;
  };
  colorGrading: {
    shadows: ColorGradingGroup;
    midtones: ColorGradingGroup;
    highlights: ColorGradingGroup;
  };
  masks: MaskDefinition[];
}

export interface MaskDefinition {
  id: string;
  type: 'sky' | 'subject' | 'foreground' | 'brush' | 'linear' | 'radial';
  visible: boolean;
  inverted: boolean;
  adjustments: Partial<Adjustments>;
}

export const INITIAL_ADJUSTMENTS: Adjustments = {
  exposure: 0,
  brightness: 0,
  contrast: 0,
  highlights: 0,
  shadows: 0,
  whites: 0,
  blacks: 0,
  temperature: 0,
  tint: 0,
  saturation: 0,
  vibrance: 0,
  toneMapper: 'agx',
  sharpness: 0,
  clarity: 0,
  dehaze: 0,
  structure: 0,
  texture: 0,
  lumaNoise: 0,
  colorNoise: 0,
  chromaticAberrationRC: 0,
  chromaticAberrationBY: 0,
  vignetteAmount: 0,
  vignetteFeather: 50,
  vignetteMidpoint: 50,
  grainAmount: 0,
  grainRoughness: 50,
  grainSize: 50,
  lutIntensity: 0,
  cropX: 0,
  cropY: 0,
  cropW: 1,
  cropH: 1,
  rotation: 0,
  flipH: false,
  flipV: false,
  lensDistortion: 0,
  lensVignette: 0,
  lensTCA: 0,
  curvePoints: {
    rgb: [[0, 0], [255, 255]],
    red: [[0, 0], [255, 255]],
    green: [[0, 0], [255, 255]],
    blue: [[0, 0], [255, 255]],
    luma: [[0, 0], [255, 255]],
  },
  hsl: {
    red: { hue: 0, sat: 0, lum: 0 },
    orange: { hue: 0, sat: 0, lum: 0 },
    yellow: { hue: 0, sat: 0, lum: 0 },
    green: { hue: 0, sat: 0, lum: 0 },
    aqua: { hue: 0, sat: 0, lum: 0 },
    blue: { hue: 0, sat: 0, lum: 0 },
    purple: { hue: 0, sat: 0, lum: 0 },
    magenta: { hue: 0, sat: 0, lum: 0 },
  },
  colorGrading: {
    shadows: { hue: 0, sat: 0, lum: 0, blending: 50, balance: 0 },
    midtones: { hue: 0, sat: 0, lum: 0, blending: 50, balance: 0 },
    highlights: { hue: 0, sat: 0, lum: 0, blending: 50, balance: 0 },
  },
  masks: [],
};
