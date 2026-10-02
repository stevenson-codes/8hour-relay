import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import './Dropdown.css'

export interface DropdownOption<T extends string> {
  value: T
  label: string
}

interface DropdownProps<T extends string> {
  value: T | ''
  onChange: (value: T) => void
  options: DropdownOption<T>[]
  placeholder?: string
  className?: string
  id?: string
}

interface MenuRect {
  top: number
  left: number
  width: number
}

function Dropdown<T extends string>({
  value,
  onChange,
  options,
  placeholder = 'Select…',
  className,
  id,
}: DropdownProps<T>) {
  const [open, setOpen] = useState(false)
  const [menuRect, setMenuRect] = useState<MenuRect | null>(null)
  const rootRef = useRef<HTMLDivElement>(null)
  const triggerRef = useRef<HTMLButtonElement>(null)
  const menuRef = useRef<HTMLUListElement>(null)

  useEffect(() => {
    if (!open) return

    function updateRect() {
      const rect = triggerRef.current?.getBoundingClientRect()
      if (!rect) return
      setMenuRect({ top: rect.bottom + 4, left: rect.left, width: rect.width })
    }

    updateRect()

    function handlePointerDown(e: MouseEvent) {
      const target = e.target as Node
      const insideTrigger = rootRef.current?.contains(target)
      const insideMenu = menuRef.current?.contains(target)
      if (!insideTrigger && !insideMenu) {
        setOpen(false)
      }
    }
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') setOpen(false)
    }

    document.addEventListener('mousedown', handlePointerDown)
    document.addEventListener('keydown', handleKeyDown)
    window.addEventListener('scroll', updateRect, true)
    window.addEventListener('resize', updateRect)
    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
      document.removeEventListener('keydown', handleKeyDown)
      window.removeEventListener('scroll', updateRect, true)
      window.removeEventListener('resize', updateRect)
    }
  }, [open])

  const selected = options.find((o) => o.value === value)
  const portalTarget = (rootRef.current?.closest('.board') as HTMLElement | null) ?? document.body

  return (
    <div className={`dropdown${className ? ` ${className}` : ''}`} ref={rootRef}>
      <button
        type="button"
        id={id}
        ref={triggerRef}
        className="dropdown-trigger"
        onClick={() => setOpen((o) => !o)}
        aria-haspopup="listbox"
        aria-expanded={open}
      >
        <span className={selected ? 'dropdown-value' : 'dropdown-placeholder'}>
          {selected ? selected.label : placeholder}
        </span>
        <svg className="dropdown-chevron" width="10" height="6" viewBox="0 0 10 6" fill="none" aria-hidden="true">
          <path
            d="M1 1l4 4 4-4"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>
      {open &&
        menuRect &&
        createPortal(
          <ul
            ref={menuRef}
            className="dropdown-menu"
            role="listbox"
            style={{ position: 'fixed', top: menuRect.top, left: menuRect.left, width: menuRect.width }}
          >
            {options.map((option) => (
              <li
                key={option.value}
                role="option"
                aria-selected={option.value === value}
                className={`dropdown-option${option.value === value ? ' selected' : ''}`}
                onMouseDown={(e) => {
                  e.preventDefault()
                  e.stopPropagation()
                  onChange(option.value)
                  setOpen(false)
                }}
              >
                {option.label}
              </li>
            ))}
          </ul>,
          portalTarget,
        )}
    </div>
  )
}

export default Dropdown
