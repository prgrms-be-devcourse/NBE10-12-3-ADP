"use client";

import { useState } from "react";

import RoughButton from "@/app/_components/RoughButton";
import RoughFrame from "@/app/_components/RoughFrame";
import { RoughInput } from "@/app/_components/RoughInput";

type WidgetGuideModalProps = {
  copiedWidgetLink: boolean;
  onCancel: () => void;
  onCopyWidgetLink: (code: string) => void;
  widgetLink: string;
};

export default function WidgetGuideModal({
  copiedWidgetLink,
  onCancel,
  onCopyWidgetLink,
  widgetLink,
}: WidgetGuideModalProps) {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const codeSnippet = `<img src="${widgetLink}" alt="내 서재 위젯" />`;

  return (
    <div
      className="rough-modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="widget-guide-title"
    >
      <div className="rough-modal-card">
        <RoughFrame className="rough-overlay" variant="card" />
        <h2 id="widget-guide-title" className="text-xl font-bold">
          README에 위젯 넣는 방법
        </h2>
        <p className="mt-2 text-sm text-gray-600">
          버튼을 눌러 단계를 바꿔가며 확인할 수 있어요.
        </p>

        <div className="mt-4 flex flex-wrap gap-2">
          <RoughButton
            roughSize="sm"
            tone={step === 1 ? "history" : "neutral"}
            type="button"
            onClick={() => setStep(1)}
          >
            1단계
          </RoughButton>
          <RoughButton
            roughSize="sm"
            tone={step === 2 ? "history" : "neutral"}
            type="button"
            onClick={() => setStep(2)}
          >
            2단계
          </RoughButton>
          <RoughButton
            roughSize="sm"
            tone={step === 3 ? "history" : "neutral"}
            type="button"
            onClick={() => setStep(3)}
          >
            3단계
          </RoughButton>
        </div>

        <div className="mt-4 rounded-lg bg-white/70 p-3 text-sm">
          {step === 1 && (
            <div>
              <div className="font-semibold">
                1. GitHub 프로필 README 레포지토리 생성
              </div>
              <div className="mt-1 text-gray-600">
                GitHub에서 사용자 이름과 같은 이름의 공개 레포지토리를 만들고,
                `README.md` 파일을 준비해 주세요.
              </div>
            </div>
          )}

          {step === 2 && (
            <div>
              <div className="font-semibold">2. 아래 코드 복사</div>
              <div className="mt-1 text-gray-600">
                프론트에서 렌더되는 결과를 그대로 붙여 넣을 수 있게 준비했습니다.
              </div>
              <div className="mt-3 flex flex-col gap-2">
                <RoughInput
                  inputClassName="text-xs"
                  roughSize="sm"
                  readOnly
                  value={codeSnippet}
                  onFocus={(e) => e.currentTarget.select()}
                />
                <div className="flex flex-wrap gap-2">
                  <RoughButton
                    roughSize="sm"
                    tone="history"
                    type="button"
                    onClick={() => onCopyWidgetLink(codeSnippet)}
                  >
                    {copiedWidgetLink ? "복사 완료" : "코드 복사"}
                  </RoughButton>
                  <span className="self-center text-xs text-gray-500">
                    프론트가 실제로 렌더하는 `img` 태그입니다.
                  </span>
                </div>
              </div>
            </div>
          )}

          {step === 3 && (
            <div>
              <div className="font-semibold">3. README.md에 붙여넣기</div>
              <div className="mt-1 text-gray-600">
                복사한 `img` 태그를 README.md에 붙여 넣으면 프로필에서 바로
                위젯이 보입니다.
              </div>
            </div>
          )}
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <RoughButton
            roughSize="sm"
            tone="cancel"
            type="button"
            onClick={onCancel}
          >
            취소
          </RoughButton>
        </div>
      </div>
    </div>
  );
}
