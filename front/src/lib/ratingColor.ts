export function ratingColor(rating: number) {
  if (rating >= 4) return "text-green-600";
  if (rating >= 2.5) return "text-sky-500";
  if (rating >= 1.5) return "text-orange-500";
  return "text-red-500";
}

export function ratingFillColor(rating: number) {
  if (rating <= 1) return "#f87171";
  if (rating <= 2.5) return "#fb923c";
  if (rating <= 4) return "#38bdf8";
  return "#4ade80";
}
