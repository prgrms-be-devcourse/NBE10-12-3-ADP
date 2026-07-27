"use client";

import { useEffect, useRef } from "react";

import rough from "roughjs/bin/rough";

import { useTheme } from "@/lib/theme/ThemeProvider";

export default function RoughBar({
  className = "",
  fill,
}: {
  className?: string;
  fill: string;
}) {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const { theme } = useTheme();

  useEffect(() => {
    const svg = svgRef.current;
    if (svg == null) return;
    const target = svg.parentElement ?? svg;

    const draw = () => {
      const { width, height } = target.getBoundingClientRect();
      if (width === 0 || height === 0) return;

      const rc = rough.svg(svg);
      const inset = 1;
      svg.setAttribute("width", `${width}`);
      svg.setAttribute("height", `${height}`);
      svg.setAttribute("viewBox", `0 0 ${width} ${height}`);
      svg.replaceChildren();

      const left = inset;
      const top = inset;
      const right = width - inset;
      const bottom = height - inset;
      const radius = Math.min(5, (right - left) / 2, (bottom - top) / 2);
      const roundedRect = [
        `M ${left + radius} ${top}`,
        `L ${right - radius} ${top}`,
        `Q ${right} ${top} ${right} ${top + radius}`,
        `L ${right} ${bottom - radius}`,
        `Q ${right} ${bottom} ${right - radius} ${bottom}`,
        `L ${left + radius} ${bottom}`,
        `Q ${left} ${bottom} ${left} ${bottom - radius}`,
        `L ${left} ${top + radius}`,
        `Q ${left} ${top} ${left + radius} ${top}`,
        "Z",
      ].join(" ");

      svg.append(
        rc.path(roundedRect, {
          stroke: theme === "dark" ? "#f4f1ea" : "#1f1f1f",
          strokeWidth: 1,
          roughness: 1.25,
          bowing: 1.1,
          fill,
          fillStyle: "solid",
        }),
      );
    };

    draw();

    const resizeObserver = new ResizeObserver(draw);
    resizeObserver.observe(target);

    return () => {
      resizeObserver.disconnect();
    };
  }, [fill, theme]);

  return <svg ref={svgRef} aria-hidden="true" className={className} />;
}
